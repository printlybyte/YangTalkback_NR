package yangTalkback.Base;

import yangTalkback.App.App;
import yangTalkback.Comm.*;
import AXLib.Utility.TH;
//线程异常处理对象
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler handler;

	public ThreadExceptionHandler() {
		this.handler = Thread.getDefaultUncaughtExceptionHandler();
	}
	//接收未处理线程
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		CLLog.Error(ex);
		if (App.IsTest && App.LastAct != null)
			App.LastAct.Alert(ex);

		// 是否抛出异常//
		if (handler != null) {
			TH.Throw(ex);
			handler.uncaughtException(thread, ex);
		}

	}
}
