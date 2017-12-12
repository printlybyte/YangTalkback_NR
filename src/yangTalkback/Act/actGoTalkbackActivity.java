package yangTalkback.Act;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import yangTalkback.App.App;

public class actGoTalkbackActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Toast.makeText(this, "点击界面过来了这个界面", Toast.LENGTH_SHORT).show();
		String key = (String) GetActivityExtraValue("_notification_param");

		if (App.LastAct instanceof actTalkback) {
			((actTalkback) App.LastAct).Quit();
		}
		finish();
		Intent intent = new Intent(this, actTalkback.class);
		intent.putExtra("_default", key);
		startActivity(intent);

	}

	// 获取上一视图传递的参数
	public Object GetActivityExtraValue(String key) {
		Intent intent = getIntent();
		if (intent == null) {
			return null;
		}
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return null;
		}

		Object object = extras.get(key);
		return object;
	}
}
