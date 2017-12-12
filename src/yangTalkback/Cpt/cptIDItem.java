package yangTalkback.Cpt;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import AXLib.Utility.EventArg;
import yangTalkback.Act.actMain;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Comm.IDModel;
import yangTalkback.Act.R;

public class cptIDItem extends cptBase implements IContainer, yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1.IGridViewItemViewCPT<IDModel> {

	private Activity _act = null;

	protected IDModel _model = null;
	@AutoRefView(id = R.item_idinfo.llLayout)
	public LinearLayout llLayout;
	@AutoRefView(id = R.item_idinfo.rlID, click = "OnClick")
	public RelativeLayout rlID;
	@AutoRefView(id = R.item_idinfo.cbID, click = "OnClick")
	public CheckBox cbID;
	@AutoRefView(id = R.item_idinfo.tvID, click = "OnClick")
	public TextView tvID;
	@AutoRefView(id = R.item_idinfo.ibSel, click = "OnClick")
	public ImageButtonEx ibSel;
	

	public cptIDItem(Activity act, IDModel model) {
		super(act);
		_act = act;
		_model = model;
	}

	public void SetAct(ActCLBase act) {
		_act = act;
	}

	@Override
	public void SetView(View v) {
		super.SetView(v);

		cbID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				boolean changed = ((actMain) _act).OnIDSelectChanged(isChecked, _model.ID);
				if (!changed)
					cbID.setChecked(!isChecked);

			}
		});
	}

	public void setControl() {
	 
		tvID.setText(String.valueOf(_model.Name));
		if (_model.IsOnLine) {
			if (_model.TalkStatus==3) {//¶Ô½²×´Ì¬
				//ºìÉ«
				tvID.setTextColor(_act.getResources().getColor(R.color.red));
			}else{
				//Ç³À¶É«
				tvID.setTextColor(_act.getResources().getColor(R.color.aaa));
			}
		} else

			tvID.setTextColor(_act.getResources().getColor(R.color.line));

		boolean sel = ((actMain) _act).SelIDList.contains((Object) _model.ID);
		cbID.setChecked(sel);

	}

	public View getContainer() {
		return llLayout;

	}

	public void OnClick(EventArg<View> arg) {
		OnClick(arg.e);
		
		if (cbID.isChecked() || _model.IsOnLine) {
			boolean changed = ((actMain) _act).OnIDSelectChanged(!cbID.isChecked(), _model.ID);
			if (changed)
				cbID.setChecked(!cbID.isChecked());

			ibSel.setVisibility(cbID.isChecked() ? View.VISIBLE : View.GONE);
		}
	}

	public IDModel getModel() {
		return _model;
	}

	public void setModel(IDModel model) {
		_model = model;
		setControl();
	}

}
