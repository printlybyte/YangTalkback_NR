package yangTalkback.Base;

import android.util.Log;
import AXLib.Utility.Console.IConsolePrint;
//������Ϣ�����
public class AndroidConsole implements IConsolePrint {

	@Override
	public void print(String tag, Object msg) {
		Log.d(tag, String.valueOf(msg));
	}

}
