package yangTalkback.Media;

import java.io.ByteArrayInputStream;

import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.WaitResult;
import AXLib.Utility.Ex.StringEx;
import yangTalkback.Comm.*;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.view.SurfaceHolder;

@SuppressLint("NewApi")
public class CameraHelper {

	// ����ǰ������ͷ
	public static int FindFrontCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				// ��������ͷ�ķ�λ��Ŀǰ�ж���ֵ�����ֱ�ΪCAMERA_FACING_FRONTǰ�ú�CAMERA_FACING_BACK����
				return camIdx;
			}
		}
		return -1;
	}

	// ���Һ�������ͷ
	public static int FindBackCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				// ��������ͷ�ķ�λ��Ŀǰ�ж���ֵ�����ֱ�ΪCAMERA_FACING_FRONTǰ�ú�CAMERA_FACING_BACK����
				return camIdx;
			}
		}
		return -1;

	}

	public static boolean HasBackCamera() {
		return FindBackCamera() != -1;
	}

	public static boolean HasFrontCamera() {
		return FindFrontCamera() != -1;
	}

	public static boolean ExistCamera(int hardwareCameraId) {
		return hardwareCameraId == FindFrontCamera() || hardwareCameraId == FindBackCamera();
	}

	public static Camera StartPreview(int hardwareCameraId, SurfaceHolder surface, int width, int height) {
		if (!ExistCamera(hardwareCameraId))
			throw new RuntimeExceptionEx("δ�ҵ�������ͷ");
		Camera cam = Camera.open(hardwareCameraId);
		if (cam == null)
			throw new RuntimeExceptionEx("δ�ҵ�������ͷ");

		Camera.Parameters parameters = cam.getParameters();
		parameters.setPictureSize(width, height);
		cam.setParameters(parameters);
		boolean preview = false;
		try {
			cam.setPreviewDisplay(surface);
			cam.startPreview();

			preview = true;

		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			if (StringEx.equalsIgnoreCase("Fail to connect to camera service", e.getMessage()))
				CLLog.Error(new CameraServiceException("��������ͷʧ��", e));
			else
				CLLog.Error(e);
		}
		return preview ? cam : null;
	}

	public static byte[] Caption(Camera cam) {
		final WaitResult<byte[]> wait = new WaitResult<byte[]>();
		ShutterCallback shutterCallback = new ShutterCallback() {
			public void onShutter() {
			}
		};
		Camera.PictureCallback picture = new Camera.PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				bais = new ByteArrayInputStream(data);
				wait.Finish(data);
			}
		};

		cam.takePicture(shutterCallback, null, picture);
		wait.Wait(1000);// �ȴ�5����

		return wait.Result;
	}

	public static void StopPreview(Camera cam) {
		try {
			cam.stopPreview();
		} catch (Exception e) {
		}
		try {
			cam.release();
			cam = null;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			CLLog.Error(e);
		}
	}

	public static byte[] Caption(int hardwareCameraId, SurfaceHolder surface, int width, int height) {
		if (!ExistCamera(hardwareCameraId))
			throw new RuntimeExceptionEx("δ�ҵ�������ͷ");

		Camera cam = Camera.open(hardwareCameraId);
		if (cam == null)
			throw new RuntimeExceptionEx("δ�ҵ�������ͷ");

		Camera.Parameters parameters = cam.getParameters();
		parameters.setPictureSize(width, height);
		cam.setParameters(parameters);
		boolean preview = false;

		try {
			cam.setPreviewDisplay(surface);
			cam.startPreview();
			preview = true;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			if (StringEx.equalsIgnoreCase("Fail to connect to camera service", e.getMessage()))
				CLLog.Error(new CameraServiceException("��������ͷʧ��", e));
			else
				CLLog.Error(e);
		}

		if (preview) {
			final WaitResult<byte[]> wait = new WaitResult<byte[]>();
			ShutterCallback shutterCallback = new ShutterCallback() {
				public void onShutter() {
				}
			};
			Camera.PictureCallback picture = new Camera.PictureCallback() {
				public void onPictureTaken(byte[] data, Camera camera) {
					ByteArrayInputStream bais = new ByteArrayInputStream(data);
					bais = new ByteArrayInputStream(data);
					wait.Finish(data);
				}
			};

			cam.takePicture(shutterCallback, null, picture);
			wait.Wait(5000);// �ȴ�5����
			try {
				cam.stopPreview();
			} catch (Exception e) {

			}
			try {
				cam.release();
				cam = null;
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				CLLog.Error(e);
			}
			return wait.Result;
		}
		return null;
	}
}