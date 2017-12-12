package yangTalkback.Act;

import AXLib.Utility.Event;
import AXLib.Utility.RuntimeExceptionEx;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class HeadSetReceiver extends BroadcastReceiver {

	public final static Event<Integer> ReceivedEvent = new Event<Integer>();

	// 重写构造方法，将接口绑定。因为此类的初始化的特殊性。
	public HeadSetReceiver() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// 获得KeyEvent对象
			KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			try {
				if (keyEvent.getAction() == KeyEvent.ACTION_UP || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					ReceivedEvent.Trigger(this, keyEvent.getAction());
					// 终止广播(不让别的程序收到此广播，免受干扰)
					//abortBroadcast();
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);

			}

		}

	}
}
