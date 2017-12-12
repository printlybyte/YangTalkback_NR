package yangTalkback.Act;

import AXLib.Utility.Ex.StringEx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
	private static boolean _isrun = false;

	// ÷ÿ–¥onReceive∑Ω∑®
	@Override
	public void onReceive(final Context context, Intent intent) {

		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals("android.intent.action.QUICKBOOT_POWERON")) {
			Log.d("BootBroadcastReceiver", "0  " + action);
			if (_isrun)
				return;
			_isrun = true;

			SharedPreferences sp = context.getApplicationContext().getSharedPreferences("_APPCONFIG_KEY", 0);
			String autorun = sp.getString("Autorun", null);
			if (StringEx.equals(autorun, "1")) {
				Log.d("BootBroadcastReceiver", "1  " + action);
				Intent ootStartIntent = new Intent(context, actLoading.class);
				ootStartIntent.putExtra("_default", "Autorun");
				ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(ootStartIntent);
			}

		}
		if (action.equals(Intent.ACTION_SHUTDOWN) || action.equals("android.intent.action.QUICKBOOT_POWEROFF")) {
			android.os.Process.killProcess(android.os.Process.myPid());

		}
	}

}
