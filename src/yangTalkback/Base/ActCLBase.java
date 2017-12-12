package yangTalkback.Base;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import java.io.EOFException;
import java.net.SocketException;

import AXLib.Model.RefObject;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ICallback;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.Act.R;
import yangTalkback.Act.actGoTalkbackActivity;
import yangTalkback.Act.actTalkback;
import yangTalkback.App.App;
import yangTalkback.App.AppConfig;
import yangTalkback.Comm.CLLog;
import yangTalkback.Comm.IDModel;
import yangTalkback.Module.Ring;
import yangTalkback.Net.ClientConnection;
import yangTalkback.Protocol.PBCallC;
import yangTalkback.Protocol.PBCallClosureC;
import yangTalkback.Protocol.PBCallR;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdM;
import yangTalkback.Protocol.PBCmdR;
import yangTalkback.Protocol.PBMedia;
import yangTalkback.Protocol.PBMonitorCloseC;
import yangTalkback.Protocol.PBMonitorCloseR;
import yangTalkback.Protocol.PBMonitorOpenC;
import yangTalkback.Protocol.PBMonitorOpenR;

//import AXVChat.Net.Model.CRequestTalk;

//视频对讲界面视图基类
public class ActCLBase extends ActAutoRefView {

	public final static int ActivityResult_Code_Talk = 0x08;
	public final static int TimeoutReconnect = 0x09;

	protected AppConfig _ac = null;
	protected ClientConnection _connection = App.GetConnection();
	protected boolean _cancelRequestTalk = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (_connection != null && _connection.getIsConnected())
			setTitle("实时对讲 ID" + _connection.ID);
		else
			setTitle("实时对讲");

		_ac = AppConfig.Instance;
		if (ScreenOrientationIsFit()) {
			onScreenReady();
		}

	}

	// android activity生成周期
	// http://www.cnblogs.com/over140/archive/2012/04/25/2331185.html
	// onCreate -> onStart -> onResume
	// onPause　->　onStop　->　onDestroy
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onScreenReady() {
	}

	@Override
	public boolean OnKeyDown_Back() {
		return super.OnKeyDown_Back();
	}

	public void finish(final int wait) {
		if (wait == 0) {
			super.finish();
		} else {
			if (!_isLoading)
				OpenLoading("正在关闭");
			ThreadEx.GetThreadHandle(new ICallback() {
				@Override
				public void invoke() {
					ThreadEx.sleep(wait);
					post(new ICallback() {
						@Override
						public void invoke() {
							finish();
						}
					});
				}
			}).start();
		}

	}

	@Override
	public void finish() {
		finish(0);
	}

	public boolean CallC(PBCallC pb, RefObject<String> msg) {
		msg.Value = "拔号的号码正忙";
		return false;
	}

	public void CallR(PBCallR pb) {

	}

	// 关闭会话
	public void CallClosureC(PBCallClosureC pb) {
	}

	public boolean MonitorOpenC(PBMonitorOpenC pb, RefObject<String> msg) {
		msg.Value = "拔号的号码正忙";
		return false;
	}

	public void MonitorOpenR(PBMonitorOpenR pb) {

	}

	public void MonitorCloseC(PBMonitorCloseC pb) {
		throw RuntimeExceptionEx.Create("not imp");
	}

	public void MonitorCloseR(PBMonitorCloseR pb) {

	}

	public void MediaPushIn(PBMedia pb) {
		// if (_connection != null && _connection.getIsConnected() && pb.To ==
		// _connection.ID)
		// actTalk.TempQueuePlay.offer(pb.Frame);
	}

	public void OnClientConnectionDisconnected() {
	 
		if (AppConfig.Instance.TimeoutReconnect) {
			OnClientConnectionTimeout();
			return;
		}
		OnClientConnectionDisconnected("与服务器通信断开，程序将退出!");
	}

	public void OnClientConnectionDisconnected(String msg) {
		AlertAndExit(msg);
	}

	public void OnClientConnectionDisconnected(Exception e) {
	 
		if (AppConfig.Instance.TimeoutReconnect) {
			OnClientConnectionTimeout();
			return;
		}
		if (AppConfig._D) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			// Alert(stack);
			// return;
		}
		if (e.getCause() != null && e.getCause() instanceof EOFException)
			OnClientConnectionDisconnected("与服务器通信出现异常，程序将退出!");
		if (e instanceof EOFException || e instanceof SocketException)
			OnClientConnectionDisconnected("与服务器通信出现异常，程序将退出!");
		else if (e instanceof RuntimeExceptionEx) {
			CLLog.Debug(e);
			OnClientConnectionDisconnected("与服务器通信出现异常，程序将退出!");
		} else {
			CLLog.Error(e);
			OnClientConnectionDisconnected("与服务器通信出现异常，程序将退出!");
		}
	}

	public void OnClientConnectionTimeout() {
 
		if (AppConfig.Instance.TimeoutReconnect) {
			OpenLoading("与服务器连接超时,正在尝试重连...");
			Intent intent = new Intent();
			setResult(TimeoutReconnect, intent);
			finish();

		} else {
			AlertAndExit("与服务器通信超时，程序将退出！");
		}
	}

	public PBCmdR OnReceiveCmdC(PBCmdC pb) {
		return null;
	}

	public void OnReceiveCmdR(PBCmdR pb) {

	}

	public void OnReceiveCmdM(PBCmdM pb) {
		if (StringEx.equals(pb.Cmd, PBCmdC.CMD_Type_TALK_Invite)) {// 邀请加入对讲
			String key = JSONHelper.forJSON(pb.JSON, String.class);
			OnTalk_Invite(pb.From, key);
		}
	}

	public void OnTalk_Invite(short from, final String key) {
		if (App.IsBack) {

			//开机后一般会停留在锁屏页面且短时间内没有进行解锁操作屏幕会进入休眠状态，此时就需要先唤醒屏幕和解锁屏幕
			//屏幕唤醒
			PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.SCREEN_DIM_WAKE_LOCK, "StartupReceiver");//最后的参数是LogCat里用的Tag
			wl.acquire();

			//屏幕解锁
			KeyguardManager km= (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
			KeyguardManager.KeyguardLock kl = km.newKeyguardLock("StartupReceiver");//参数是LogCat里用的Tag
			kl.disableKeyguard();
			Intent intent=new Intent(this,actGoTalkbackActivity.class);
			intent.putExtra("_notification_param",key);
			startActivity(intent);

			Ring.Play(R.raw.duanxin);
//			String msg = String.format("%s邀请您加入对讲，是否加入?", GetNameByID(from));
//			NotifyManage.notify("点击加入对讲，时间:" + TimeUtil.ToString(TimeUtil.getCurrentUtilDate(), TimeUtil.YYYY_SECOND), msg, key, this, actGoTalkbackActivity.class);
		} else {

//			this.Prompt(String.format("%s邀请您加入对讲，是否加入?", GetNameByID(from)), PromptButton.NO, new IAction<Prompt.PromptButton>() {
//				public void invoke(PromptButton obj) {
//					if (obj == PromptButton.YES) {
//						DoEnterTalkback(key);
//					}
//				}
//			});
			post(new ICallback() {

				@Override
				public void invoke() {
					// TODO Auto-generated method stub
					DoEnterTalkback(key);
				}
			});
		}
	}

	// 进入对讲
	public void DoEnterTalkback(String key) {
		startActivity(actTalkback.class, key);
	}

	public String GetNameByID(ListEx<Short> ids) {
		String name = "";
		for (short id : ids) {
			name += (name == "" ? "" : ",") + ((ActCLBase) this).GetNameByID(id);

		}
		return name;
	}

	public String GetNameByID(final short id) {

		if (_connection != null && _connection.getIsConnected()) {
			ListEx<IDModel> ids = _connection.GetAllIDByCache();
			String name = "";
			IDModel model = ids.FirstOrDefault(new Predicate<IDModel>() {
				@Override
				public boolean Test(IDModel obj) {
					return obj.ID == id;

				}
			});
			if (model != null) {
				if (StringEx.isEmpty(model.Name))
					return String.valueOf(model.ID);
				else
					return model.Name;
			} else {
				return String.valueOf(model.ID);
			}
		}
		return String.valueOf(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == TimeoutReconnect) {
			Intent intent1 = new Intent();
			setResult(TimeoutReconnect, intent1);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, intent);

	}

}
