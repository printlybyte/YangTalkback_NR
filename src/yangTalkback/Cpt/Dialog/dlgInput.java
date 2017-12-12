package yangTalkback.Cpt.Dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import AXLib.Utility.EventArg;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Cpt.IContainer;
import yangTalkback.Cpt.cptBase;
import yangTalkback.Act.R;

public class dlgInput extends dlgBase {
	private cptInput _cpt;

	public dlgInput(ActCLBase act, String title) {
		super(act, title);

	}

	@Override
	protected IContainer GetContainer() {
		if (this._cpt == null)
			this._cpt = new cptInput(_act);
		return this._cpt;
	}

	protected void OnSubmit(String str) {
		SubmitEvent.Trigger(this, str);
	}

	public void SetValue(String value) {
		if (value != null)
			_cpt.SetValue(value);
	}

	public class cptInput extends cptBase {

		@AutoRefView(id = R.dig_input.tbInput)
		public EditText tbInput;

		@AutoRefView(id = R.dig_input.btOK, click = "btOK_Click")
		public Button btOK;

		public cptInput(Context ctx) {
			super(ctx);

		}

		@Override
		public void SetView(View v) {
			super.SetView(v);
			InitControls();
		}

		public void InitControls() {

		}

		public void btOK_Click(EventArg<View> arg) {
			OnSubmit(this.tbInput.getText().toString());
			dlgInput.this.dismiss();
		}

		public void btCancel_Click(EventArg<View> arg) {
			dlgInput.this.cancel();
		}

		public void SetValue(String value) {
			tbInput.setText(value);
		}

	}

}
