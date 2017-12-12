package yangTalkback.Act;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import java.io.Serializable;
import java.util.List;

import AXLib.Model.KeyValue;
import AXLib.Utility.CallBack;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.MapEx;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.App.App;
import yangTalkback.Base.ActBase;
import yangTalkback.Module.Ring;

/**
 * 监听程序是否在前后台运行Service
 * 
 * @Description: 监听程序是否在前后台运行Service
 * 
 * @FileName: AppStatusService.java
 * 
 * @Package com.test.service
 * 
 * @Author Hanyonglu
 * 
 * @Date 2012-4-13 下午04:13:47
 * 
 * @Version V1.0
 */
public class AppStatusService extends Service {

	public static AppStatusService Instance = null;

	public static final String TAG = "AppStatusService";
	private ActivityManager activityManager;
	private String packageName;
	private boolean isStop = false;
	private static Thread _checkThread = null;
	private static Thread _ringThread = null;
	private static Object _lock = new Object();
	private int _ringTimeSpan = 0;
	private int _notifyId = 1;
	private MapEx<Integer, String> _notifications = new MapEx<Integer, String>();

	@Override
	public IBinder onBind(Intent intent) {
		Instance = this;
		return null;

	}

	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Instance = this;
		activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		packageName = this.getPackageName();
		synchronized (_lock) {
			if (_checkThread == null) {
				_checkThread = ThreadEx.GetThreadHandle(new CallBack(this, "CheckForegroundThread"), "前后台检测线程");
				_checkThread.start();
			}
			// _ringThread = ThreadEx.GetThreadHandle(new CallBack(this,
			// "RingThread"), "消息通知线程");
			// _ringThread.start();
		}

		return super.onStartCommand(intent, flags, startId);

	}

	public void CheckForegroundThread() {
		boolean isForeground = true;
		while (!isStop && !App.IsAppExit) {
			try {

				if (isAppOnForeground()) {
					if (!isForeground)
						App.ForegroundChange(!isForeground);
					isForeground = true;
				} else {
					// showNotification();
					if (isForeground)
						App.ForegroundChange(!isForeground);
					isForeground = false;
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				if (e instanceof RuntimeExceptionEx)
					throw (RuntimeExceptionEx) e;
				else
					throw RuntimeExceptionEx.Create("检测前后台线程出错", e);
			}
			_checkThread = null;
			ThreadEx.sleep(1000);
		}
	}

	public void RingThread() {
		while (!isStop && !App.IsAppExit) {
			ThreadEx.sleep(100);
			if (_ringTimeSpan > 0) {
				Ring.Play(R.raw.duanxin);
				_ringTimeSpan--;
				continue;
			}

		}
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public boolean isAppOnForeground() {
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDestroy() {
		if (_checkThread != null)
			_checkThread.stop();
		System.out.println("终止服务");
		super.onDestroy();
		cancelNotification();
		isStop = true;
	}

	public void showNotification(String msg, String title, Serializable param, ActBase act, Class<?> cls) {
		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

		// 定义Notification的各种属性
		Notification notification = new Notification(R.drawable.icon, title, System.currentTimeMillis());
		// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		// 点击后自动清除Notification
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.ledARGB = Color.BLUE;
		notification.ledOnMS = 5000;

		// 设置通知的事件消息
		CharSequence contentTitle = title; // 通知栏标题
		CharSequence contentText = msg; // 通知栏内容

		Intent notificationIntent = new Intent(act, cls);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);// 关键的一步，设置启动模式
		notificationIntent.putExtra("_notification_param", param);

		notificationIntent.setAction(String.valueOf(System.currentTimeMillis()));

		PendingIntent contentIntent = PendingIntent.getActivity(act, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(act, contentTitle, contentText, contentIntent);
		int id = _notifyId++;
		synchronized (_notifications) {
			for (KeyValue<Integer, String> kv : _notifications) {
				if (StringEx.equals(title, kv.Value)) {
					id = kv.Key;
					break;
				}
			}
			_notifications.Set(id, title);
		}
	 
		// 把Notification传递给NotificationManager
		notificationManager.notify(id, notification);
		// _ringTimeSpan = 100;
	}

	public void cancelNotification(String title) {
		int id = -1;
		synchronized (_notifications) {
			for (KeyValue<Integer, String> kv : _notifications) {
				if (StringEx.equals(title, kv.Value)) {
					id = kv.Key;
					break;
				}
			}
		}
		if (id == -1)
			return;
		NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}

	// 取消通知
	public void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		for (int i = 0; i < _notifyId; i++)
			notificationManager.cancel(i);
		_ringTimeSpan = 0;

	}
}