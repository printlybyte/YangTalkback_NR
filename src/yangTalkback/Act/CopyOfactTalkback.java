package yangTalkback.Act;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.HashMap;

import AXLib.Utility.AQueue;
import AXLib.Utility.CallBack;
import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.IAction;
import AXLib.Utility.ICallback;
import AXLib.Utility.ISelect;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.MapEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Base.Prompt;
import yangTalkback.Base.Prompt.PromptButton;
import yangTalkback.Codec.Cfg.AudioEncodeCfg;
import yangTalkback.Codec.MicEncoder;
import yangTalkback.Codec.MicEncoder.MicEncoderDataReceiver;
import yangTalkback.Comm.CLLog;
import yangTalkback.Comm.IDModel;
import yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1;
import yangTalkback.Cpt.cptMenu;
import yangTalkback.Cpt.itemTalkbackInfo;
import yangTalkback.Media.AudioPlay;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.MediaFrame.MediaFrameCommandType;
import yangTalkback.Net.Model.TalkbackChannelInfo;
import yangTalkback.Net.Model.TalkbackStatusInfo;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdR;
import yangTalkback.Protocol.PBMedia;

@AutoRefView(id = R.layout.act_talkback, layout = 0x03)
public class CopyOfactTalkback extends ActGenDataViewActivity1<TalkbackStatusInfo> implements MicEncoderDataReceiver {

	@AutoRefView(id = R.act_talkback.cptMenu)
	public cptMenu cptMenu = new cptMenu(this);
	@AutoRefView(id = R.act_talkback.btTalk, touch = "btTalk_Touch")
	public Button btTalk;// 退出按钮
	@AutoRefView(id = R.act_talkback.btQuit, click = "btQuit_Click")
	public Button btQuit;// 退出按钮
	@AutoRefView(id = R.act_talkback.gvGrid)
	public GridView gvGrid;// 显示号码列表

	private ListEx<IDModel> _SysIDList = new ListEx<IDModel>();// 保存系统的号码列表
	private ListEx<Short> _selIDList = new ListEx<Short>();
	private String _key = null;
	private boolean _isTalkbackReqing = false;
	private MicEncoder _me = null;

	private MapEx<Short, AudioPlay> _mapAudioPlayer = null;

	private boolean _isTalking = false;// 当前是否按下讲话按钮
	private boolean _isPlaying = false;// 当前是否正在接收其他人发过来的语音
	private boolean _isNeedSendMediaCommandStart = false;// 是否需要发送开始语音
	private boolean _isNeedSendMediaCommandStop = false;// 是否需要发送停止语音
	private AQueue<MediaFrame> _qPushOut = new AQueue<MediaFrame>();
	private AQueue<PBMedia> _qPushIn = new AQueue<PBMedia>();
	private Thread _threadPushOut = null;
	private Thread _threadPushIn = null;
	private Thread _threadRefresh = null;
	private TalkbackChannelInfo _info = null;
	private ListEx<TalkbackStatusInfo> _statusList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public void onScreenReady() {
		if (_connection == null) {
			AlertAndExit("网络连接异常！");
			return;
		}
		_key = GetActivityDefaultExtraValue(true);
		if (_key == null)
			return;
		InitControls();
	}

	public void InitControls() {

		cptMenu.ExecutionEvent.add(this, "cptMenu_ExecutionEvent");
		cptMenu.SetActiveMenu("Talkback");

		_me = new MicEncoder(AudioEncodeCfg.GetDefault(), this);
		_me.start();

		this.CallByNewThread("Enter");// 启用新线程调用方法
		_threadPushOut = this.CallByNewThread("PushOutThread");
		_threadPushIn = this.CallByNewThread("PushInThread");
	}

	// 进入对讲
	public void DoEnterTalkback(String key) {
		finish();
		super.DoEnterTalkback(key);

	}

	/** 进入对讲 */
	public void Enter() {
		try {
			OpenLoading("正在进入对讲", false, null);
			if (_connection != null && _connection.getIsLogined()) {
				PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_Enter, JSONHelper.toJSON(_key));
				PBCmdR pbr = _connection.CmdC(pbc);
				if (pbr == null)
					throw RuntimeExceptionEx.Create("_connection.CmdC(pbc)==null");
				if (!pbr.Result) {
					AlertAndOut(pbr.Message);
					return;
				}
				_SysIDList = _connection.GetAllIDByCache();// 获取所有号码
				// 获取对讲信息
				pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_Info, JSONHelper.toJSON(_key));
				pbr = _connection.CmdC(pbc);
				_info = JSONHelper.forJSON(pbr.JSON, TalkbackChannelInfo.class);
				// 过滤出在对讲通道的ID
				_SysIDList = _SysIDList.Where(new Predicate<IDModel>() {
					public boolean Test(IDModel obj) {
						return _info.OriginalIDList.contains((Object) obj.ID);
					}
				});
				if (_mapAudioPlayer == null) {
					_mapAudioPlayer = new MapEx<Short, AudioPlay>();
					for (IDModel model : _SysIDList) {
						AudioPlay ap = new AudioPlay(_ac.SpeakMode, true);
						ap.Start();
						_mapAudioPlayer.Set(model.ID, ap);

					}
				}
				_statusList = _SysIDList.Select(new ISelect<IDModel, TalkbackStatusInfo>() {
					public TalkbackStatusInfo Select(IDModel t) {
						TalkbackStatusInfo status = new TalkbackStatusInfo();
						status.IDModel = t;
						if (_info.ActiveIDList.contains(t.ID))
							status.JoinStatus = 1;
						else if (_info.AvailableIDList.contains(t.ID))
							status.JoinStatus = 0;
						else
							status.JoinStatus = -1;

						return status;
					}
				});

				InitGridViewActivity(gvGrid, 2, R.layout.item_talkback_info);
				_threadRefresh = CallByNewThread("RefreshThread");
			}
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			CLLog.Error(e);
			AlertAndOut("进入对讲失败");
		} finally {
			CloseLoading();
		}
	}

	public void RefreshThread() {
		while (!this.IsFinished && !this.isFinishing() && _connection.getIsConnected()) {
			try {
				PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_Info, JSONHelper.toJSON(_key));
				PBCmdR pbr = _connection.CmdC(pbc);
				_info = JSONHelper.forJSON(pbr.JSON, TalkbackChannelInfo.class);
				// 过滤出在对讲通道的ID
				_SysIDList = _SysIDList.Where(new Predicate<IDModel>() {
					public boolean Test(IDModel obj) {
						return _info.OriginalIDList.contains((Object) obj.ID);
					}
				});
				_statusList = _SysIDList.Select(new ISelect<IDModel, TalkbackStatusInfo>() {
					public TalkbackStatusInfo Select(IDModel t) {
						TalkbackStatusInfo status = new TalkbackStatusInfo();
						status.IDModel = t;
						if (_info.ActiveIDList.contains(t.ID))
							status.JoinStatus = 1;
						else if (_info.AvailableIDList.contains(t.ID))
							status.JoinStatus = 0;
						else
							status.JoinStatus = -1;
						return status;
					}
				});
				Reflash();

			} catch (Exception e) {

			}
			ThreadEx.sleep(3000);
		}
	}

	/** 退出对讲 */
	public void Leave() {
		try {
			OpenLoading("正在退出对讲", false, null);
			if (_connection != null && _connection.getIsLogined()) {
				PBCmdC pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_Leave, JSONHelper.toJSON(_key));
				PBCmdR pbr = _connection.CmdC(pbc);
			}
		} catch (Exception e) {

		} finally {
			CloseLoading();
		}
	}

	@Override
	public boolean OnKeyDown_Back() {
		return false;
	}

	@Override
	public void finish() {
		_me.stop();
		if (_mapAudioPlayer != null)
			for (AudioPlay ap : _mapAudioPlayer.GetValues())
				ap.Stop();

		_me = null;
		ThreadEx.stop(_threadPushOut);
		ThreadEx.stop(_threadPushIn);
		ThreadEx.stop(_threadRefresh);
		_threadPushOut = null;
		_threadPushIn = null;
		_threadRefresh = null;
		super.finish();
	}

	public void cptMenu_ExecutionEvent(final EventArg<Object> arg) {
		if (!StringEx.equalsIgnoreCase(arg.e.toString(), "Talkback")) {
			Prompt("是否退出实时对讲？", PromptButton.NO, new IAction<Prompt.PromptButton>() {
				@Override
				public void invoke(PromptButton obj) {
					if (obj == PromptButton.YES) {
						ThreadEx.ThreadCall(new ICallback() {
							public void invoke() {
								Leave();
								finish();
								if (StringEx.equalsIgnoreCase(arg.e.toString(), "Record")) {
									startActivity(actRecord.class);
								}
							}
						});
					}
				}
			});

		}

	}

	// 获取数据项
	public ListEx<TalkbackStatusInfo> getData(int index) {
		if (index == 1)
			return _statusList;
		else
			return new ListEx<TalkbackStatusInfo>();
	}

	public void btTalk_Touch(EventArg<MotionEvent> arg) {
		if (_isPlaying)
			return;
		if (arg.e.getAction() == MotionEvent.ACTION_DOWN) {
			_isTalking = true;
			_isNeedSendMediaCommandStart = true;
		} else if (arg.e.getAction() == MotionEvent.ACTION_UP) {
			_isTalking = false;
			_isNeedSendMediaCommandStop = true;
		}
	}

	public void btQuit_Click(EventArg<View> arg) {
		Prompt("是否退出实时对讲？", PromptButton.NO, new IAction<Prompt.PromptButton>() {
			@Override
			public void invoke(PromptButton obj) {
				if (obj == PromptButton.YES) {
					Leave();
					finish();
				}
			}
		});
	}

	@Override
	protected ActGenDataViewActivity1.IGridViewItemViewCPT<TalkbackStatusInfo> CreateItem(TalkbackStatusInfo model) {
		return new itemTalkbackInfo(_act, model);
	}

	// 列表中按钮点击事件
	public void ItemClickEvent(EventArg<TalkbackStatusInfo> arg) {

	}

	public void SwitchTalkbackStatus() {
		post(new ICallback() {
			public void invoke() {
				btTalk.setEnabled(!_isPlaying);
			}
		});
	}

	public void MediaPushIn(PBMedia pb) {
		_qPushIn.Enqueue(pb);
	}

	@Override
	public void Received(MediaFrame frame) {

		if (_isTalking)
			_qPushOut.Enqueue(frame);

	}

	public void PushOutThread() {
		while (!this.IsFinished && !this.isFinishing() && _connection.getIsConnected()) {
			PBMedia pb = null;
			if (_isNeedSendMediaCommandStart) {
				pb = new PBMedia(_connection.ID, (short) 0, MediaFrame.CreateCommandMediaFrame(true, MediaFrame.MediaFrameCommandType.Start));
				_connection.PushMedia(pb);
				_isNeedSendMediaCommandStart = false;
			}
			if (_qPushOut.size() > 0) {

				MediaFrame frame = _qPushOut.Dequeue();
				pb = new PBMedia(_connection.ID, (short) 0, frame);
				_connection.PushMedia(pb);

			} else {
				ThreadEx.sleep(10);
			}
			if (_isNeedSendMediaCommandStop) {
				pb = new PBMedia(_connection.ID, (short) 0, MediaFrame.CreateCommandMediaFrame(true, MediaFrame.MediaFrameCommandType.Stop));
				_connection.PushMedia(pb);
				_isNeedSendMediaCommandStop = false;
			}
		}
	}

	public void PushInThread() {
		while (!this.IsFinished && !this.isFinishing() && _connection.getIsConnected()) {
			if (!_isTalking && _qPushIn.size() > 0) {
				PBMedia pb = _qPushIn.Dequeue();
				MediaFrame mf = pb.Frame;
				if (mf.IsCommandFrame()) {
					MediaFrameCommandType ct = mf.GetCommandType();
					if (ct == MediaFrameCommandType.Start) {
						_isPlaying = true;
						SwitchTalkbackStatus();
					} else if (ct == MediaFrameCommandType.Stop) {
						_isPlaying = false;
						SwitchTalkbackStatus();
					}
				} else {
					AudioPlay ap = _mapAudioPlayer.Get(pb.From);
					if (_isPlaying && ap != null)
						ap.Play(mf);

				}

			} else {
				ThreadEx.sleep(10);
			}

		}

	}

	public void SetTalkButtonEnabled(boolean isEnabled) {

	}

	public static class TalkbackPlayCtrl {
		private int _speekMode = 0;
		private CopyOfactTalkback _act;
		private short _fromId = -1;
		private boolean _isworking = false;
		private boolean _isTwoway = false;
		private boolean _isBtnDown = false;
		private boolean _twowayCanPlay = false;
		private boolean _sinwayCanPlay = false;
		private HashMap<Short, AudioPlay> _apMap = new HashMap<Short, AudioPlay>();
		private ListEx<Short> _twowayIDList = new ListEx<Short>();
		private AQueue<PBMedia> _qPBCache = new AQueue<PBMedia>();
		private TalkbackChannelInfo _chInfo = null;
		private Thread _playThread = null;

		public TalkbackPlayCtrl(CopyOfactTalkback act, short fromId, boolean isTwoway, int speekMode) {
			_act = act;
			_fromId = fromId;
			_speekMode = speekMode;
			_isTwoway = isTwoway;
		}

		public void SetTalkbackChannelInfo(TalkbackChannelInfo info) {
			_chInfo = info;
			_twowayIDList = info.TwowayIDList.ToList();
		}

		public void PushIn(PBMedia pb) {
			_qPBCache.Enqueue(pb);
		}

		public void PushIn_Twoway(PBMedia pb) {

		}

		public void PushIn_Sinway(PBMedia pb) {
			MediaFrame mf = pb.Frame;
			AudioPlay ap = _apMap.get(pb.From);
			if (_twowayIDList.contains((Object) pb.From)) {// 如果讲话人使用双向

				// 如果当前讲话按钮按下，等讲完话后再播对方声音
				while (!_twowayCanPlay)
					ThreadEx.sleep(10);

				if (!mf.IsCommandFrame())// 过滤命令包
					ap.Play(mf);

			} else {
				if (mf.IsCommandFrame()) {
					if (mf.GetCommandType() == MediaFrameCommandType.Start) {
						_sinwayCanPlay = true;
						_act.SetTalkButtonEnabled(false);
					} else if (mf.GetCommandType() == MediaFrameCommandType.Stop) {
						_sinwayCanPlay = false;
						_act.SetTalkButtonEnabled(true);
					}
				} else {
					if (_sinwayCanPlay)
						ap.Play(mf);
				}

			}
		}

		public void PlayThread() {
			while (_isworking) {
				if (_qPBCache.size() > 0) {
					PBMedia pb = _qPBCache.Dequeue();
					if (_isTwoway)
						PushIn_Twoway(pb);
					else
						PushIn_Sinway(pb);

				}
			}
		}

		public void SetTalkButtonDownStatus(boolean isDown) {
			_isBtnDown = isDown;
			_twowayCanPlay = !isDown;// 按下的时候不允许播对方双向
		}

		public void Start() {
			if (_isworking)
				return;
			_isworking = true;
			for (short id : _chInfo.OriginalIDList) {
				_apMap.put(id, new AudioPlay(_speekMode, true));
			}
			_playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"));
			_playThread.start();
		}

		public void Stop() {
			if (!_isworking)
				return;
			_isworking = false;
			ThreadEx.stop(_playThread);
			_playThread = null;
			_qPBCache.clear();
		}
	}

}
