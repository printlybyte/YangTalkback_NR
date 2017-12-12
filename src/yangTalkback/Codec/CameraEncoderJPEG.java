package yangTalkback.Codec;

import java.io.ByteArrayOutputStream;

import AXLib.Utility.CallBack;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import AXLib.Utility.Ex.StringEx;
import yangTalkback.App.AppConfig;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Comm.*;
import yangTalkback.Media.MediaFrame;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

//图片编码方式采集器
@SuppressLint({ "NewApi", "NewApi" })
public class CameraEncoderJPEG extends CameraEncoder {

	private Camera _cam = null;// 摄像头
	private Thread _workThread = null;// 工作线程
	private boolean _isWorking = false;// 工作 状态
	private int _lastOptions = 100;// 最后压缩比
	private int _frameIndex = 0;// 帧序
	private int _maxPicSize = 1024 * 5;// 图片最大尺寸
	private Queue<byte[]> _queueData = new Queue<byte[]>();// 采集数据队列,没用
	private Size _previewSize;// 预览分辨率
	private int _previewFmt;// 预览格式
	private int _maxFrameRate = 10;// 最大帧率
	private byte[] _previewData = null;// 当前预览图片数据

	public CameraEncoderJPEG(VideoEncodeCfg cfg, int restartSpan, CameraEncoderDataReceiver receiver) {
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

	// 定焦
	@Override
	public void AutoFocus() {
		if (_cam != null) {
			_cam.autoFocus(null);
		}
	}

	// 参数设置
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
					// _previewSize.width = encCfg.width;
					// _previewSize.height = encCfg.height;
				}

				_cam.setPreviewCallback(previewCallback);
				_cam.setPreviewDisplay(encCfg.holder);
				_cam.startPreview();
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				if (StringEx.equalsIgnoreCase("Fail to connect to camera service", e.getMessage()))
					CLLog.Error(new CameraServiceException("连接摄像头失败", e));
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
				ThreadEx.sleep(1000 / _maxFrameRate);
			}
		}
	}

	// 采集完一帧图片引发
	private void OnCaped(byte[] buf) {
		if (buf == null)
			return;
		MediaFrame frame = null;
		buf = compressImage(buf);
		if (buf != null) {
			if (_frameIndex++ % 60 == 0)
				frame = MediaFrame.createVideoKeyFrame(encCfg, System.currentTimeMillis(), buf, 0, buf.length);
			else
				frame = MediaFrame.CreateVideoFrame(encCfg, System.currentTimeMillis(), buf, 0, buf.length);
			receiver.Received(frame);
		}
	}

	// 压缩图片
	private byte[] compressImage(byte[] data) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		YuvImage image = new YuvImage(data, _previewFmt, _previewSize.width, _previewSize.height, null);
		image.compressToJpeg(new Rect(0, 0, _previewSize.width, _previewSize.height), _lastOptions, baos);
		if (_lastOptions > AppConfig.Instance.PicModeQuality * 10) {
			while (baos.size() > _maxPicSize) {
				_lastOptions -= 10;
				baos.reset();
				image.compressToJpeg(new Rect(0, 0, _previewSize.width, _previewSize.height), _lastOptions, baos);
				if (_lastOptions <= AppConfig.Instance.PicModeQuality * 10)
					break;

			}
		}
		byte[] buf = baos.toByteArray();
		try {
			baos.close();
		} catch (Exception e) {

		}

//		Console.d("compressImage", buf.length);
		return buf;
	}

	public void Restart() {

	}
}