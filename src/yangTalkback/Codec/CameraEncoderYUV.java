package yangTalkback.Codec;

import AXLib.Utility.CallBack;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import AXLib.Utility.Ex.StringEx;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Codec.FFCodec.FFObj;
import yangTalkback.Comm.*;
import yangTalkback.Media.MediaFrame;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

//2014-05-03–ﬁ∏ƒ∫ÛŒ¥≤‚ ‘
@SuppressLint({ "NewApi", "NewApi" })
public class CameraEncoderYUV extends CameraEncoder {

	protected Camera _cam = null;
	protected Thread _workThread = null;
	protected boolean _isWorking = false;

	protected int _frameIndex = 0;
	protected int _maxPicSize = 1024 * 5;
	protected Queue<byte[]> _queueData = new Queue<byte[]>();
	protected Size _previewSize;
	protected int _previewFmt;
	protected int _maxFrameRate = 20;
	protected byte[] _previewData = null;
	protected int _lastOptions = 100;
	protected FFObj _ffEnc;

	public CameraEncoderYUV(VideoEncodeCfg cfg, int restartSpan, CameraEncoderDataReceiver receiver) {
		super(cfg, restartSpan, receiver);

	}

	PreviewCallback previewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			_previewData = data;

		}
	};

	public void start() {
		if (_isWorking)
			return;
		_isWorking = true;
		setup();
		_workThread = ThreadEx.GetThreadHandle(new CallBack(this, "WorkThread"));
		_workThread.start();
	}

	public void stop() {
		if (!_isWorking)
			return;
		_isWorking = false;
		ThreadEx.waitStop(_workThread, 1000);
		_workThread = null;
		release();

	}

	@Override
	public void AutoFocus() {
		if (_cam != null) {
			_cam.autoFocus(null);
		}
	}

	private void setup() {
		if (_cam == null) {
			try {
				_cam = Camera.open(encCfg.cameraId);
				Camera.Parameters parameters = _cam.getParameters();
				_previewFmt = parameters.getPreviewFormat();
				_previewSize = parameters.getPreviewSize();
				if (_previewSize.width != encCfg.width || _previewSize.height != encCfg.height) {
					parameters.setPreviewSize(encCfg.width, encCfg.height);
					_cam.setParameters(parameters);
					_previewSize = parameters.getPreviewSize();

				}

				_cam.setPreviewCallback(previewCallback);
				_cam.setPreviewDisplay(encCfg.holder);
				_cam.startPreview();
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				if (StringEx.equalsIgnoreCase("Fail to connect to camera service", e.getMessage()))
					CLLog.Error(new CameraServiceException("¡¨Ω”…„œÒÕ∑ ß∞‹", e));
				else
					CLLog.Error(e);
				stop();
			}
		}
	}

	private void release() {
		if (_cam != null) {
			try {
				_cam.setPreviewCallback(null);
				_cam.stopPreview();

			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
			}
			try {
				_cam.release();
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
			}
		}
	}

	public void WorkThread() {
		while (_isWorking) {
			if (_cam != null) {
				// _previewData=data;
				byte[] buf = _previewData;
				if (buf != null)
					OnCaped(buf);
				// ThreadEx.sleep(1000 / _maxFrameRate);
			}
		}
	}

	protected void OnCaped(byte[] buf) {
		if (buf == null)
			return;
		MediaFrame frame = null;
		buf = compressImage(buf);
		if (buf != null) {
			if (_frameIndex++ % 60 == 0)
				frame = MediaFrame.createVideoKeyFrame(encCfg, 0, buf, 0, buf.length);
			else
				frame = MediaFrame.CreateVideoFrame(encCfg, 0, buf, 0, buf.length);
			receiver.Received(frame);
		}

	}

	protected byte[] compressImage(byte[] data) {
		return data;

	}

	public static void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height) {
		if (yuv420sp == null || yuv420 == null)
			return;
		int framesize = width * height;
		int i = 0, j = 0;
		// copy y
		for (i = 0; i < framesize; i++) {
			yuv420[i] = yuv420sp[i];
		}
		i = 0;
		for (j = 0; j < framesize / 2; j += 2) {
			yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
			i++;
		}
		i = 0;
		for (j = 1; j < framesize / 2; j += 2) {
			yuv420[i + framesize] = yuv420sp[j + framesize];
			i++;
		}
	}

	public void Restart() {

	}

}