package com.example.volumekey;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

public class DetectThread extends Thread{
	
	
	Context mContext;
	Timer mTimer;
	TimerTask mTimerTask;
	int Id;
	int keyCode;
	boolean isStop = false;
	
	CallbackFucntion cb;
	
	public DetectThread(int id, Context context) {
		mTimer = new Timer("checkLongPress");
		Id = id;
		mContext = context;
		
		cb = (VolumeService) context;
	}
	
	
	public void run () {
		//Log.v("gengj=============", "Thread ID " + Id + "銆�tart");
		// TODO Auto-generated method stub
		int fd = JNIClass.init(Id);
		if (fd <= 0) {
			//Log.v("gengj============", "jni init failed");
			return;
		}
		while (!isStop) {
			int res = JNIClass.isKeyDown(fd); 					
			if (res == 1 || res == 2) {
				startTimer(res);
				cb.VolumeButtonPressCb(res);
			} else if (res == 0){
				stopTimer();
				cb.VolumeButtonReleaseCb(res);
			}
		
		}
		
		JNIClass.unInit(fd);

		//Log.v("gengj============", "Stop thread " + Id);
	}

	public void stopThread() {
		isStop = true;
	}
	
	private void startTimer(int key) {
		if (mTimerTask != null) {
			mTimerTask.cancel();
		}
		
		mTimerTask = new MyTimerTask((VolumeService)mContext, mTimer, 500, key);
	}
	
	private void stopTimer() {
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

}
