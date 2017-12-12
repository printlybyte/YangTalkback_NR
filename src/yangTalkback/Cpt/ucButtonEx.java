package yangTalkback.Cpt;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import AXLib.Utility.EventArg;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Act.R;

public class ucButtonEx extends cptBase implements IContainer {
	private int _imgResid = 0;
	private String _text = null;
	private ucButtonExStyle _style = null;

	@AutoRefView(id = R.uc_buttonex.llCtn, click = "llCtn_Click", touch = "llCtn_Touch")
	public cptLinearLayoutEx llCtn;
	@AutoRefView(id = R.uc_buttonex.ibButton, click = "ibButton_Click", touch = "ibButton_Touch")
	public ImageButtonEx ibButton;
	@AutoRefView(id = R.uc_buttonex.tvText, click = "tvText_Click", touch = "tvText_Touch")
	public TextView tvText;

	public ucButtonEx(int imgResid, String text) {
		this(imgResid, text, ucButtonExStyle.getDefault());

	}

	public ucButtonEx(int imgResid, String text, ucButtonExStyle style) {
		super(null);
		_imgResid = imgResid;
		_text = text;
		_style = style;
	}

	@Override
	public void SetView(View v) {
		super.SetView(v);
		SetImageResId(_imgResid);

		tvText.setText(_text);
		SetStyle(_style);
	}

	public void SetStyle(ucButtonExStyle style) {
		_style = style;

		if (_style.fontSize != -1)
			tvText.setTextSize(_style.fontSize);
		if (_style.fontColor != -1)
			tvText.setTextColor(_style.fontColor);

		if (_style.minHeight != -1)
			llCtn.setMinimumHeight(_style.minHeight);
		if (_style.minWidth != -1)
			llCtn.setMinimumWidth(_style.minWidth);

		if (_style.backgroupResId != -1)
			llCtn.setBackgroundResource(_style.backgroupResId);
		else if (_style.backgroupColor != -1)
			llCtn.setBackgroundColor(_style.backgroupColor);
		else
			llCtn.setBackgroundColor(0x00000000);

		if (_style.borderWidth != -1 && _style.borderColor != -1)
			llCtn.setBorder(_style.borderWidth, _style.borderColor);

		ibButton.setScaleType(style.imageScaleType);

	}

	public void SetImageResId(int resId) {
		ibButton.setImageResource(resId);
		_imgResid = resId;
	}

	public void SetText(String txt) {
		tvText.setText(txt);
	}

	public void OnTouch(MotionEvent event) {
		super.OnTouch(event);
		if (getEnabled()) {
			if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				if (_style.backgroupResId != -1)
					llCtn.setBackgroundResource(_style.backgroupResId);
				else if (_style.backgroupColor != -1)
					llCtn.setBackgroundColor(_style.backgroupColor);
				else
					llCtn.setBackgroundColor(0x00000000);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (_style.touchDownBackgroupResId != -1)
					llCtn.setBackgroundResource(_style.touchDownBackgroupResId);
				else if (_style.touchDownBackgroupColor != -1)
					llCtn.setBackgroundColor(_style.touchDownBackgroupColor);
				else
					llCtn.setBackgroundColor(0x22000000);
			}
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		tvText.setEnabled(enabled);
		ibButton.setEnabled(enabled);
	}

	public static class ucButtonExStyle {
		public int minWidth = -1;
		public int minHeight = -1;
		public int fontSize = -1;
		public int fontColor = -1;
		public int touchDownBackgroupColor = -1;
		public int touchDownBackgroupResId = -1;
		public int backgroupResId = -1;
		public int backgroupColor = -1;
		public ImageView.ScaleType imageScaleType = ImageView.ScaleType.FIT_CENTER;
		public int borderWidth = -1;
		public int borderColor = -1;

		public static ucButtonExStyle getDefault() {
			ucButtonExStyle style = new ucButtonExStyle();
			return style;
		}

	}

	public void llCtn_Click(EventArg<View> arg) {
		OnClick(arg.e);
	}

	public void ibButton_Click(EventArg<View> arg) {
		OnClick(arg.e);
	}

	public void tvText_Click(EventArg<View> arg) {
		OnClick(arg.e);
	}

	public void llCtn_Touch(EventArg<MotionEvent> arg) {
		OnTouch(arg.e);

	}

	public void ibButton_Touch(EventArg<MotionEvent> arg) {
		OnTouch(arg.e);
	}

	public void tvText_Touch(EventArg<MotionEvent> arg) {
		OnTouch(arg.e);
	}

}
