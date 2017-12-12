package yangTalkback.Cpt;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Act.R;

public class cptMenu extends cptBase implements IContainer {

	@AutoRefView(id = R.cpt_menu.rlTalkback, click = "rlTalkback_Click")
	public RelativeLayout rlTalkback;
	@AutoRefView(id = R.cpt_menu.rlMain, click = "rlMain_Click")
	public RelativeLayout rlMain;
	@AutoRefView(id = R.cpt_menu.rlRecord, click = "rlRecord_Click")
	public RelativeLayout rlRecord;

	@AutoRefView(id = R.cpt_menu.tvTalkback, click = "rlTalkback_Click")
	public TextView tvTalkback;
	@AutoRefView(id = R.cpt_menu.tvMain, click = "rlMain_Click")
	public TextView tvMain;
	@AutoRefView(id = R.cpt_menu.tvRecord, click = "rlRecord_Click")
	public TextView tvRecord;

	private String _activeMenu = null;

	public void rlTalkback_Click(EventArg<View> arg) {
		OnExecution("Talkback");
	}

	public void rlMain_Click(EventArg<View> arg) {
		OnExecution("Main");
	}

	public void rlRecord_Click(EventArg<View> arg) {
		OnExecution("Record");
	}

	@Override
	protected void OnExecution(Object obj) {

		// SetActiveMenu(obj.toString());
		super.OnExecution(obj);
	}

	public void SetActiveMenu(String obj) {
		if (StringEx.equalsIgnoreCase(_activeMenu, obj.toString()))
			return;

		int bg = R.drawable.c_border_bottom1;
		int color = 0xFF333333;

		int bg_active = R.drawable.c_border_bottom3;
		int color_active = 0xFF33B1C1;

		rlTalkback.setBackgroundResource(0);
		tvTalkback.setBackgroundResource(0);
		rlMain.setBackgroundResource(0);
		tvMain.setBackgroundResource(0);
		rlRecord.setBackgroundResource(0);
		tvRecord.setBackgroundResource(0);

		if (StringEx.equalsIgnoreCase("Talkback", obj.toString())) {
			tvTalkback.setBackgroundResource(R.drawable.m_bottom_active);
			rlTalkback.setBackgroundResource(R.drawable.m_bottom_bg1);
		}
		if (StringEx.equalsIgnoreCase("Main", obj.toString())) {
			tvMain.setBackgroundResource(R.drawable.m_bottom_active);
			rlMain.setBackgroundResource(R.drawable.m_bottom_bg1);

		}
		if (StringEx.equalsIgnoreCase("Record", obj.toString())) {
			tvRecord.setBackgroundResource(R.drawable.m_bottom_active);
			rlRecord.setBackgroundResource(R.drawable.m_bottom_bg1);
		}

		_activeMenu = obj.toString();
	}

	public cptMenu(ActCLBase act) {
		super(act);

	}

	@Override
	public void SetView(View v) {
		super.SetView(v);

	}

}