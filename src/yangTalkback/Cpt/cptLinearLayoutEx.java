package yangTalkback.Cpt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

@SuppressLint({ "DrawAllocation", "DrawAllocation" })
public class cptLinearLayoutEx extends LinearLayout {

	private int _borderWidth = -1;
	private int _borderColor = -1;

	public cptLinearLayoutEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public cptLinearLayoutEx(Context context) {
		super(context);

	}

	public void setBorder(int width, int color) {
		_borderWidth = width;
		_borderColor = color;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		if (_borderWidth != -1 && _borderColor != -1) {
			Rect rec = canvas.getClipBounds();
			rec.bottom--;
			rec.right--;
			Paint paint = new Paint();
			paint.setColor(_borderColor);
			paint.setStrokeWidth(_borderWidth);

			paint.setStyle(Paint.Style.STROKE);
			canvas.drawRect(rec, paint);
		}

	}

}
