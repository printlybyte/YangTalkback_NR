package yangTalkback.Act;

import android.os.Bundle;
import android.widget.Spinner;

import AXLib.Utility.EventArg;
import AXLib.Utility.MapEx;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Cpt.cptDDLEx;

@AutoRefView(id = R.layout.act_main, layout = 0x03)
public class actTest extends ActCLBase {

	@AutoRefView(id = R.act_main.ddlChannel)
	public Spinner ddlChannel;
	public cptDDLEx<String> cptDDL = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		InitControls();
	}

	public void InitControls() {

		cptDDL = new cptDDLEx<String>(this, ddlChannel);
		cptDDL.Selected.add(this, "cptDDl_Selected");
		MapEx<String, String> tab = new MapEx<String, String>();
		tab.Set("1", "1");
		tab.Set("2", "2");
		tab.Set("3", "3");
		cptDDL.setSource(tab);
		cptDDL.Open();
	}

	public void cptDDl_Selected(EventArg<String> arg) {

	}
}
