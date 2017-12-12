package yangTalkback.Media;

import AXLib.Utility.Event;
import AXLib.Utility.RuntimeExceptionEx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

//视频显示控件封装
public class VideoImage extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder _sh = null;
	private boolean _ScaleChanged = false;
	public Bitmap bmp = null;

	private static Matrix _matrix = new Matrix();
	public ScaleMode Scale = ScaleMode.Fit;

	public Event<View> ClickEvent;// = new Event<View>();
	public Event<String> SurfaceEvent;// = new Event<String>();

	public VideoImage(Context context) {
		super(context);
	}

	public VideoImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		BindHolder(getHolder());

		if (!this.isInEditMode()) {
			ClickEvent = new Event<View>();
			SurfaceEvent = new Event<String>();
		}
	}

	@Override
	public SurfaceHolder getHolder() {
		if (_sh == null)
			return super.getHolder();
		else {
			return _sh;
		}

	}

	private void BindHolder(SurfaceHolder sh) {
		_sh = sh;
		sh.addCallback(this);
		// sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!this.isInEditMode()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				ClickEvent.Trigger(this, this);
			}
		}
		return super.onTouchEvent(event);
	}

	public void Play(Bitmap bmp) {
		this.bmp = bmp;
		drawImg(bmp);
	}

	public void Play(VideoDisplayFrame vdFrame) {
		Play(vdFrame.BMP);
	}

	// 画图方法
	protected void drawImg(Bitmap bmp) {
		if (_sh == null)
			return;
		Canvas canvas = _sh.lockCanvas();
		if (canvas == null)
			return;
		try {
			if (bmp != null) {
				// 生成合适的图像
				// canvas.drawBitmap(bmp, null, null);
				// Rect rect = new Rect(0, 0, 352, 288);
				// canvas.drawBitmap(bmp, null, rect, null);

				if (this.Scale == ScaleMode.FullScreen)
					canvas.drawBitmap(bmp, null, canvas.getClipBounds(), null);
				else if (this.Scale == ScaleMode.Fit) {
					Rect rect = canvas.getClipBounds();
					rect = ScaleSize(rect, bmp.getWidth(), bmp.getHeight());
					canvas.drawARGB(0xff, 00, 00, 00);
					canvas.drawBitmap(bmp, null, rect, null);
				} else if (this.Scale == ScaleMode.Original) {
					canvas.drawBitmap(bmp, null, canvas.getClipBounds(), null);
				}

			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			_sh.unlockCanvasAndPost(canvas);
		}
		// if (_ScaleChanged) {
		// _ScaleChanged = false;
		// drawImg();
		//
		// }

	}

	// 画图方法
	private void drawImg() {
		if (_sh == null)
			return;
		Canvas canvas = _sh.lockCanvas();
		if (canvas == null)
			return;
		try {

			// canvas.drawColor(0x000000);
			// canvas.drawARGB(0xff, 00, 00, 00);
			int w = this.getWidth();
			int h = this.getHeight();

			Bitmap bmp1 = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			int w1 = bmp1.getWidth();
			Rect rect = canvas.getClipBounds();
			canvas.drawBitmap(bmp1, null, canvas.getClipBounds(), null);
			bmp1.recycle();
			bmp1 = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

			canvas.drawBitmap(bmp1, null, canvas.getClipBounds(), null);
			bmp1.recycle();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			_sh.unlockCanvasAndPost(canvas);
		}

	}

	// 缩放并保持比例
	protected Rect ScaleSize(Rect rect, int sWidth, int sHeight) {
		int tWidth = rect.width();
		int tHeight = rect.height();

		float sProportion = (float) sWidth / (float) sHeight;// 原始比例
		float tProportion = (float) tWidth / (float) tHeight;// 目标比例
		if (tProportion > 1) {// 宽大
			tWidth = (int) (((float) tHeight / (float) sHeight) * sWidth);
		} else {
			tHeight = (int) (((float) tWidth / (float) sWidth) * sHeight);
		}

		int tTop = (rect.height() - tHeight) / 2;
		int tLeft = (rect.width() - tWidth) / 2;
		return new Rect(tLeft, tTop, tLeft + tWidth, tTop + tHeight);
	}

	public void Clean() {
		int w = this.getWidth();
		int h = this.getHeight();
		try {
			Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
			drawImg(bmp);
			bmp.recycle();
		} catch (Exception ex) {
			String stack = RuntimeExceptionEx.GetStackTraceString(ex);
		}
	}

	// 缩放图片
	private Bitmap getReduceBitmap(Bitmap bitmap, int w, int h) {

		int width = bitmap.getWidth();
		int hight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float wScake = ((float) w / width);
		float hScake = ((float) h / hight);
		matrix.postScale(wScake, hScake);
		return Bitmap.createBitmap(bitmap, 0, 0, width, hight, matrix, true);
	}

	public void SetScaleMode(ScaleMode mode) {

		this.Scale = mode;

		this.Scale = mode;
		_ScaleChanged = true;
	}

	public ScaleMode GetScaleMode() {

		return this.Scale;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (!this.isInEditMode()) {

			BindHolder(holder);
			SurfaceEvent.Trigger(this, "surfaceChanged");
		}

		// _surface = _sh.getSurface();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!this.isInEditMode()) {
			BindHolder(holder);
			SurfaceEvent.Trigger(this, "surfaceCreated");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (!this.isInEditMode()) {
			_sh = null;
			SurfaceEvent.Trigger(this, "surfaceDestroyed");
		}
	}

	public static enum ScaleMode {
		// 原始尺寸
		Original((int) 0),
		// 适合尺寸
		Fit((int) 1),
		// 满屏
		FullScreen((int) 2);

		private int id;

		private ScaleMode(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

}
