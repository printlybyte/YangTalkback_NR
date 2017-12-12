package com.example.volumekey;

import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;

public class MyTimerTask extends TimerTask{
	
	CallbackFucntion cb;
	Timer mTimer;
	
	int keyCode; //1:Up, 2:Down, 0: release
	
	public MyTimerTask(CallbackFucntion owner, Timer timer, long delay, int key) {
		cb = owner;
		mTimer = timer;
		keyCode = key;
		mTimer.schedule(this, delay);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		cb.LongPressCb(keyCode);
	}

}
