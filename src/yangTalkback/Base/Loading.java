package yangTalkback.Base;

import AXLib.Utility.*;
import yangTalkback.App.App;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Message;

//loading封装
public class Loading {
	private ActBase _act = null;
	private ProgressDialog _pd = null;
	@SuppressLint("HandlerLeak")
	private static Handler _handler = null;

	private ICallback _cancelCallBack = null;

	public Loading(ActBase act) {
		_act = act;
		_pd = new ProgressDialog(_act);

		if (_handler == null) {
			_handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (_pd != null)
						_pd.dismiss();
				}
			};
		}
	}

	public void Open(String msg) {

		Open(msg, true, null);
	}

	public void Open(String msg, boolean cancelable, ICallback cancelCallBack) {
		if (Thread.currentThread() != App.UIThread)
			throw new RuntimeExceptionEx("非UI线程调用Loading.Open");
		_cancelCallBack = cancelCallBack;
		_pd.setTitle("请稍候...");
		_pd.setMessage(msg);
		_pd.setCancelable(cancelable);
		_pd.setCanceledOnTouchOutside(false);
		_pd.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (_cancelCallBack != null)
					_cancelCallBack.invoke();
			}
		});
		try {
			_pd.show();
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
	}

	public void Close() {
		if (Thread.currentThread() != App.UIThread)
			throw new RuntimeExceptionEx("非UI线程调用Loading.Close");
		try {
			_pd.dismiss();
		} catch (Exception e) {

		}
		// _handler.sendEmptyMessage(0);
	}

}
