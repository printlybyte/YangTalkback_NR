package yangTalkback.Cpt;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import AXLib.Utility.EventArg;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Net.Model.AudioFileModel;
import yangTalkback.Act.R;

public class itemRecord extends cptBase implements IContainer, yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1.IGridViewItemViewCPT<AudioFileModel> {

	@AutoRefView(id = R.item_record.llLayout)
	public LinearLayout llLayout;
	@AutoRefView(id = R.item_record.tvTime)
	public TextView tvTime;
	@AutoRefView(id = R.item_record.tvSpan)
	public TextView tvSpan;
	@AutoRefView(id = R.item_record.tvIDList)
	public TextView tvIDList;
	@AutoRefView(id = R.item_record.btPlay, click = "btPlay_Click")
	public Button btPlay;

	private Activity _act = null;
	private boolean _controlSeted = false;
	protected AudioFileModel _model = null;

	public itemRecord(Activity act, AudioFileModel model) {
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

	}

	public void setControl() {
		tvTime.setText("时间:" + _model.BegTime);
		tvSpan.setText("成员:" + ((ActCLBase) _act).GetNameByID(Short.parseShort(_model.MainID)) + "  时长:" + _model.Span);
		String name = "";
		for (short id : _model.IDList) {
			name += (name == "" ? "" : ",") + ((ActCLBase) _act).GetNameByID(id);
		}
		tvIDList.setText("参与:" + name);
	}

	public View getContainer() {
		return llLayout;

	}

	public void OnClick(EventArg<View> arg) {
		OnClick(arg.e);
	}

	public void btPlay_Click(EventArg<View> arg) {
		OnExecution("Play");
	}

	public AudioFileModel getModel() {
		return _model;
	}

	public void setModel(AudioFileModel model) {
		_model = model;
		setControl();
	}

}
