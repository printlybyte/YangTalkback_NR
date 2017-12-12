package yangTalkback.Codec;

import AXLib.Utility.Event;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.StringEx;

import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Comm.*;
import yangTalkback.Media.CameraHelper;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;

//摄像头采集基类
@SuppressLint("NewApi")
public abstract class CameraEncoderBase implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, Camera.ErrorCallback {

	public final Event<Exception> Error = new Event<Exception>();
	protected Camera camera;
	protected VideoEncodeCfg encCfg = null;

	// 设置采集摄像头
	protected void setCamera(MediaRecorder mr, VideoEncodeCfg cfg) {
		if (cfg.cameraId == -1)
			throw new IllegalArgumentException("cameraId wrong");
		int cid = cfg.cameraId;
		if (camera == null) {
			int backCamera = FindBackCamera();
			int frontCamera = FindFrontCamera();

			if (!(frontCamera == cid || backCamera == cid)) {// 如果摄像头不等于前后置则为异常
				String camName = "";
				if (cfg.cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
					camName = "后置";
				if (cfg.cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
					camName = "前置";
				throw RuntimeExceptionEx.Create(String.format("未能找到%s摄像头", camName));
			}
			try {
				camera = Camera.open(cid);
			} catch (Exception ex) {
				String string = RuntimeExceptionEx.GetStackTraceString(ex);
				CLLog.Error(ex);
				//
				if (StringEx.equalsIgnoreCase("Fail to connect to camera service", ex.getMessage()))
					OnError(new CameraServiceException("打开摄像头失败", ex));
				else
					throw RuntimeExceptionEx.Create(String.format("打开%d摄像头", cid), ex);
				stop();
			}
			if (camera == null)
				throw RuntimeExceptionEx.Create("未能打开摄像头");
			Parameters parameters = camera.getParameters();
			camera.setParameters(parameters);
			camera.setErrorCallback(this);
			camera.setDisplayOrientation(cfg.Orientation);// 设置角度
			camera.stopPreview();
			camera.unlock();

		}

		mr.setCamera(camera);
		mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		//
		// if (encCfg.cameraId != -1)
		// mr.setVideoSource(encCfg.cameraId);
		// else
		// mr.setCamera(encCfg.camera);
		//
	}

	public abstract void start();

	public abstract void stop();

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		String msgString = "MEDIA_RECORDER_ERROR_UNKNOWN";
		if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
			msgString = "MEDIA_RECORDER_ERROR_UNKNOWN";
			// Info.Trigger(this, msgString);
		}

	}

	// 当异常引发
	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		String msgString = null;
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN)
			msgString = "MEDIA_RECORDER_INFO_UNKNOWN";
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
			msgString = "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED";
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
			msgString = "MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED";

		OnError("MediaRecorder error:" + what + "  " + msgString);

	}

	// 当异常引发
	@Override
	public void onError(int error, Camera camera) {
		OnError("camera error:" + error);
	}

	// 当异常引发
	protected void OnError(String msg) {
		OnError(new RuntimeExceptionEx(msg));
	}

	// 当异常引发
	protected void OnError(Exception e) {
		if (Error.getHandleCount() > 0)
			Error.Trigger(this, e);
	}

	protected long getCurrentTimeMillis() {
		return 1000 / encCfg.frameRate;
		// return System.currentTimeMillis();
	}

	// 查找前置摄像头
	public static int FindFrontCamera() {
		return CameraHelper.FindFrontCamera();

	}

	// 查找后置摄像头
	public static int FindBackCamera() {
		return CameraHelper.FindBackCamera();

	}

}
