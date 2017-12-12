package com.example.volumekey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import android.R.integer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class VolumeService extends Service implements CallbackFucntion {

	public final static int THREAD_NUM = 20;

	PowerManager pm;
	WakeLock mWakeLock;

	List<DetectThread> mThreads = new ArrayList<DetectThread>();

	private CallBackInBG callBack;

	public static boolean isRunning = false;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return new MyBinder(this);
	}

	@Override
	public void onCreate() {
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gengj");

		mWakeLock.acquire();

		for (int i = 0; i < THREAD_NUM; i++) {
			mThreads.add(new DetectThread(i, this));
		}

		startDetectThreads();
		super.onCreate();
	}

//	public int onStartCommand(Intent intent, int flags, int startId) {
//		super.onStartCommand(intent, flags, startId);
//
//		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//
//		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gengj");
//
//		mWakeLock.acquire();
//
//		for (int i = 0; i < THREAD_NUM; i++) {
//			mThreads.add(new DetectThread(i, this));
//		}
//
//		startDetectThreads();
//		isRunning = true;
//		return 0;
//	}

	public void onDestroy() {
		super.onDestroy();
		stopThreads();
		mWakeLock.release();
		isRunning = false;
	}

	private void startDetectThreads() {
		Iterator<DetectThread> iterator = mThreads.iterator();

		while (iterator.hasNext()) {
			iterator.next().start();
		}
	}

	private void stopThreads() {
		Iterator<DetectThread> iterator = mThreads.iterator();

		while (iterator.hasNext()) {
			iterator.next().stopThread();
		}
	}

	@Override
	public void LongPressCb(int key) {
		// TODO Auto-generated method stub
		String keyCode = "";
		if (key == 1) {
			keyCode = "Up";
		} else if (key == 2) {
			keyCode = "Down";
			callBack.down();
		}

		LogFile.writeFile("Long Pressed Volume Button: " + keyCode);
	}

	@Override
	public void VolumeButtonPressCb(int key) {
		// TODO Auto-generated method stub
		String keyCode = "";
		if (key == 1) {
			keyCode = "Up";
		} else if (key == 2) {
			keyCode = "Down";
		}

		LogFile.writeFile("Pressed Volume Button: " + keyCode);
	}

	@Override
	public void VolumeButtonReleaseCb(int key) {
		// TODO Auto-generated method stub
		String keyCode = "";
		// if (key == 1) {
		// keyCode = "Up";
		// } else if(key == 2) {
		// keyCode = "Down";
		// }
		callBack.up();

		LogFile.writeFile("Released Volume Button: " + keyCode);
	}

	public void SetCallBackInBG(CallBackInBG callBack) {
		this.callBack = callBack;
	}

	public class MyBinder extends Binder {

		private VolumeService mservice;

		public MyBinder(VolumeService service) {
			this.mservice = service;
		}

		public VolumeService getService() {
			return mservice;
		}
	}

}
