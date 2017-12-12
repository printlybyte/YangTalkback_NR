package yangTalkback.Act;

import android.os.Bundle;

import AXLib.Utility.Ex.StringEx;
import yangTalkback.App.App;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;

@AutoRefView(id = R.layout.act_loading, layout = 0x03)
public class actLoading extends ActCLBase {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		
		super.onCreate(savedInstanceState);

		if (1 == 2) {
			startActivity(actTest.class);
		} else {
			String flag = (String) GetActivityDefaultExtraValue();
			if (StringEx.equalsIgnoreCase("Autorun", flag)) {
				if (_ac.IsAutorun) {
					startActivity(actLogin.class, flag);
				} else {
					App.exit();
				}
			} else {
				startActivity(actLogin.class);
			}
		}
		finish();
	}

}
