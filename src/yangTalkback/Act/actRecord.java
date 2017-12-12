package yangTalkback.Act;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import AXLib.Model.KeyValue;
import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ICallback;
import AXLib.Utility.ISelect;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.LittleEndianDataInputStream;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import AXLib.Utility.TimeUtil;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Comm.CLLog;
import yangTalkback.Comm.IDModel;
import yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1;
import yangTalkback.Cpt.ImageButtonEx;
import yangTalkback.Cpt.cptMenu;
import yangTalkback.Cpt.itemRecord;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.RecordAudioPlay;
import yangTalkback.Media.RecordAudioPlay.IAudioPlayEventHandle;
import yangTalkback.Net.Model.AudioFileModel;
import yangTalkback.Net.Model.TalkbackChannelInfo;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdR;

@AutoRefView(id = R.layout.act_record, layout = 0x03)
public class actRecord extends ActGenDataViewActivity1<AudioFileModel> implements IAudioPlayEventHandle {

	@AutoRefView(id = R.act_record.cptMenu)
	public cptMenu cptMenu = new cptMenu(this);

	@AutoRefView(id = R.act_record.gvGrid)
	public GridView gvGrid;
	@AutoRefView(id = R.act_record.tbDay)
	public EditText tbDay;
	@AutoRefView(id = R.act_record.ibSearch, click = "ibSearch_Click")
	public ImageButtonEx ibSearch;
	@AutoRefView(id = R.act_record.ivPlaying)
	public ImageView ivPlaying;
	@AutoRefView(id = R.act_record.pbLoading)
	public ProgressBar pbLoading;

	private ListEx<IDModel> _SysIDList = new ListEx<IDModel>();// 保存系统的号码列表
	private ListEx<AudioFileModel> _dataList = new ListEx<AudioFileModel>();
	private ListEx<AudioFileModel> dataList = new ListEx<AudioFileModel>();

	private boolean _isTalkbackReqing = false;
	private Date _time = TimeUtil.getCurrentUtilDate();
	private Object _playLock = new Object();
	private RecordAudioPlay _ap = null;
	private String _lastPlayFile = null;
	private String _lastPlayBase64Data = null;
	private Short loginID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onScreenReady() {
		loginID = Short.parseShort(_ac.LoginID);
		if (_connection == null) {
			AlertAndExit("网络连接异常！");
		}
		// 禁止输入法自动弹出
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		InitControls();

	}

	public void InitControls() {

		tbDay.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					final Calendar cd = Calendar.getInstance();
					Date date = new Date();
					if (StringEx.isEmpty(tbDay.getText().toString()))
						cd.setTime(date);
					else {
						date = TimeUtil.ToDate(tbDay.getText().toString(), TimeUtil.YYYY_MM_DD);
						cd.setTime(date);
					}
					new DatePickerDialog(actRecord.this, new OnDateSetListener() {
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							tbDay.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
							_time = TimeUtil.ToDate(tbDay.getText().toString(), "yyyy-MM-dd");
							// CallByNewThread("LoadFileList");// 启用新线程调用方法
						}
					}, cd.get(Calendar.YEAR), cd.get(Calendar.MONTH), cd.get(Calendar.DAY_OF_MONTH)).show();

				}
				return false;
			}
		});
		cptMenu.ExecutionEvent.add(this, "cptMenu_ExecutionEvent");
		cptMenu.SetActiveMenu("Record");
		this.CallByNewThread("LoadAllID");// 启用新线程调用方法
	}

	// 加载所有号码
	public void LoadAllID() {
		if (_connection != null && _connection.getIsLogined()) {
			_SysIDList = _connection.GetAllID();// 获取所有号码

			LoadFileList();
			InitGridViewActivity(gvGrid, 1, R.layout.item_record, -1, Tools.DensityUtil.dip2px(this, 70));

		}
	}

	public void LoadFileList() {
		try {
			OpenLoading("正在加载数据", false, null);
			if (_connection != null && _connection.getIsLogined()) {
				PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_RECORD_List, JSONHelper.toJSON(_time));
				PBCmdR pbr = _connection.CmdC(pbc);
				if (pbr == null)
					throw RuntimeExceptionEx.Create("_connection.CmdC(pbc)==null");
				if (!pbr.Result) {
					AlertAndOut(pbr.Message);
					return;
				}
				Type token = (new TypeToken<ListEx<AudioFileModel>>() {
				}).getType();
				_dataList = JSONHelper.forJSON(pbr.JSON, token);
				for (AudioFileModel audioFileModel : _dataList) {
					if (audioFileModel.IDList.contains(loginID)) {
						dataList.add(audioFileModel);
					}
				}
				if (_grid != null)
					Reflash();
			}
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			CLLog.Error(e);
			AlertAndOut("加载数据失败");
		} finally {
			CloseLoading();
		}
	}

	@Override
	public void finish() {
		if (_ap != null) {
			_ap.Stop();
		}
		super.finish();
	}

	@Override
	public boolean OnKeyDown_Back() {

		return true;
	}

	public void Play(final AudioFileModel model) {

		ThreadEx.ThreadPoolCall(new ICallback() {
			public void invoke() {

				String base64 = null;
				if (_connection != null && _connection.getIsLogined()) {
					try {
						if (_ap != null)
							_ap.Stop();
						post(new ICallback() {
							public void invoke() {
								pbLoading.setVisibility(View.VISIBLE);
							}
						});
						if (!StringEx.equalsIgnoreCase(model.File, _lastPlayFile)) {
							synchronized (_playLock) {
								PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_RECORD_Get, model.File);
								PBCmdR pbr = _connection.CmdC(pbc);
								if (pbr == null)
									return;
								if (!pbr.Result) {
									Notice(pbr.Message);
									return;
								}
								base64 = pbr.JSON;
								_lastPlayFile = model.File;
								_lastPlayBase64Data = base64;
							}
						} else {
							base64 = _lastPlayBase64Data;
						}
						post(new ICallback() {
							public void invoke() {
								pbLoading.setVisibility(View.GONE);
							}
						});
						synchronized (_playLock) {
							if (!StringEx.isEmpty(base64))
								Play(base64);
						}
					} finally {
						post(new ICallback() {
							public void invoke() {
								pbLoading.setVisibility(View.GONE);
							}
						});
					}
				}
			}
		});
	}

	public void Play(String base64) {

		byte[] bytes = android.util.Base64.decode(base64, Base64.DEFAULT);
		ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
		AXLib.Utility.LittleEndianDataInputStream is = new LittleEndianDataInputStream(bs);
		ListEx<MediaFrame> fs = new ListEx<MediaFrame>();
		try {
			while (is.available() > 0) {
				int len = is.readInt();
				byte[] buf = is.readFully(len);
				MediaFrame mf = new MediaFrame(buf);
				fs.add(mf);
			}
		} catch (Exception e) {
			throw RuntimeExceptionEx.Create(e);
		}
		try {
			if (_ap != null)
				_ap.Stop();
			_ap = new RecordAudioPlay(_ac.SpeakMode, this);
			_ap.Start();
		} catch (Exception e) {

		}
		if (_ap != null) {
			MediaFrame[] mfs = new MediaFrame[fs.size()];
			fs.toArray(mfs);
			_ap.Play(mfs);
		}

	}

	public void LoadMyChannel() {

		try {
			OpenLoading("正在获取对讲信息", false, null);
			if (_connection != null && _connection.getIsLogined()) {
				PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_MyChannel, "");
				PBCmdR pbr = _connection.CmdC(pbc);
				if (pbr != null && pbr.Result) {
					java.lang.reflect.Type token = (new TypeToken<ListEx<TalkbackChannelInfo>>() {
					}).getType();
					ListEx<TalkbackChannelInfo> list = JSONHelper.forJSON(pbr.JSON, token);
					if (list.size() == 0) {
						Alert("当前没有您参与的对讲");
						return;
					}
					if (list.size() == 1) {
						finish();
						startActivity(actTalkback.class, list.get(0).Key);
						return;
					}
					final ListEx<KeyValue<String, String>> tab = list.Select(new ISelect<TalkbackChannelInfo, KeyValue<String, String>>() {
						public KeyValue<String, String> Select(TalkbackChannelInfo t) {
							return new KeyValue<String, String>(t.Key, "参加成员：" + StringEx.ConstituteString(t.OriginalIDList));
						}
					});
					post(new ICallback() {
						@Override
						public void invoke() {
							finish();
							// 这里要做弹出选择的，先使用默认最后一个的方式
							startActivity(actTalkback.class, tab.get(tab.size() - 1).Key);
							return;
							// _isFirstDDLSel = true;
							// tab.insertElementAt(new KeyValue<String,
							// String>(t.Key, "参加成员：" +
							// StringEx.ConstituteString(t.OriginalIDList)), 0);
							// cptDDL.setSource(tab);
							// cptDDL.Open();
						}
					});

				}
			}
		} catch (Exception e) {
			CLLog.Error(e);
			OpenLoading("获取对讲信息失败", false, null);
		} finally {
			CloseLoading();
		}
	}

	// 获取数据项
	public ListEx<AudioFileModel> getData(int index) {
		if (index == 1)
			return dataList;
		else
			return new ListEx<AudioFileModel>();
	}

	public void cptMenu_ExecutionEvent(EventArg<Object> arg) {
		cptMenu.SetActiveMenu(arg.e.toString());
		if (StringEx.equalsIgnoreCase(arg.e.toString(), "Main")) {
			finish();
		}
		if (StringEx.equalsIgnoreCase(arg.e.toString(), "Talkback")) {
			CallByNewThread("LoadMyChannel");
			// startActivity(actTalkback.class);
		}
	}

	@Override
	protected ActGenDataViewActivity1.IGridViewItemViewCPT<AudioFileModel> CreateItem(AudioFileModel model) {
		return new itemRecord(_act, model);
	}

	@Override
	public void ItemExecutionEvent(EventArg<Object> arg) {
		AudioFileModel model = ((itemRecord) arg.sender).getModel();
		Play(model);
	}

	// 列表中按钮点击事件
	public void ItemClickEvent(EventArg<AudioFileModel> arg) {
		AudioFileModel model = ((itemRecord) arg.sender).getModel();

	}

	public void ibSearch_Click(EventArg<View> arg) {

		CallByNewThread("LoadFileList");// 启用新线程调用方法
	}

	@Override
	public void PlayBegin() {
		post(new ICallback() {
			@Override
			public void invoke() {
				ivPlaying.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void PlayEnd() {
		post(new ICallback() {
			@Override
			public void invoke() {
				ivPlaying.setVisibility(View.GONE);
			}
		});
	}
}
