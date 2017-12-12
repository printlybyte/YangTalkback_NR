package yangTalkback.Cpt;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class ImageButtonEx extends ImageButton {

	private Drawable _drawable = null;
	private boolean _transparentBackgroup = false;
	public boolean IsGray = false;

	public ImageButtonEx(Context context) {
		super(context);
	}

	public ImageButtonEx(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.isEnabled()) {
			 
			if (_transparentBackgroup) {
				if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
					this.setBackgroundColor(0x00000000);

				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					this.setBackgroundColor(0x22000000);
				}
			 
			}
		 
 
			// this.setScaleType(ScaleType.FIT_XY);
			return super.onTouchEvent(event);
		} else {
			return false;
		}
	}

	@Override
	public void setEnabled(boolean status) {
		if (_drawable == null)
			_drawable = this.getDrawable();
		if (!status && this.isEnabled()) {
			Drawable drawable = _drawable;
			drawable.mutate();
			ColorMatrix cm = new ColorMatrix();
			cm.setSaturation(0);
			ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
			drawable.setColorFilter(cf);
			super.setImageDrawable(drawable);
		} else if (status && !this.isEnabled()) {
			Drawable drawable = _drawable;
			drawable.clearColorFilter();
			super.setImageDrawable(drawable);
			// this.setScaleType(ScaleType.FIT_XY);

		}
		super.setEnabled(status);
	}

	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		if (drawable != null)
			_drawable = drawable;
	}

	public void setGray(boolean status) {
		if (_drawable == null)
			_drawable = this.getDrawable();
		if (status) {
			Drawable drawable = _drawable;
			drawable.mutate();
			ColorMatrix cm = new ColorMatrix();
			cm.setSaturation(0);
			ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
			drawable.setColorFilter(cf);
			super.setImageDrawable(drawable);

		} else {
			Drawable drawable = _drawable;
			drawable.clearColorFilter();
			super.setImageDrawable(drawable);
		}
		IsGray = status;
	}

	public void SetTransparentBackgroup() {
		_transparentBackgroup = true;
		this.setBackgroundColor(0x00000000);
	}

}
