package yangTalkback.Base;

import yangTalkback.App.AppConfig;

import android.app.Application;
import android.content.SharedPreferences;

import AXLib.Utility.Ex.Config.GeneralConfig;

//参数配置器,用来保存系统的一些参数
public class AndroidConfig extends GeneralConfig {
	private Application Application;

	public AndroidConfig(Application _Application) {
		Application = _Application;
	}

	@Override
	public void Save(String key, String str) {
		SharedPreferences sp = getSharedPreferences();
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, str);
		editor.commit();

	}

	@Override
	public String Read(String key) {
		SharedPreferences sp = getSharedPreferences();
		return sp.getString(key, null);
	}

	private SharedPreferences getSharedPreferences() {
		return Application.getApplicationContext().getSharedPreferences(AppConfig.KEY_APP, 0);
	}

}
