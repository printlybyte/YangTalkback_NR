package yangTalkback.Codec;

import yangTalkback.Media.CameraHelper;
import android.annotation.SuppressLint;
import android.media.CamcorderProfile;

import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;

//摄像头编码参数
@SuppressLint("NewApi")
public class CamVideoCoderProfile {
	public boolean IsSystemDefault;
	public boolean IsHardwareDefault;
	public int HardwareCameraId = -1;
	public ResolutionType VideoSize = ResolutionType.GetDefault();
	public int FrameRate;
	public int BitRate;

	public void setVideoSize(int width, int height) {
		for (ResolutionType type : ResolutionType.values()) {
			if (type.getWidth() == width && type.getHeight() == height) {
				VideoSize = type;
				return;
			}
		}

		VideoSize = ResolutionType.GetDefault();
	}

	public CamVideoCoderProfile Copy() {
		try {
			CamVideoCoderProfile profile = new CamVideoCoderProfile();
			profile.IsSystemDefault = this.IsSystemDefault;
			profile.IsHardwareDefault = this.IsHardwareDefault;
			profile.HardwareCameraId = this.HardwareCameraId;
			profile.VideoSize = this.VideoSize;
			profile.FrameRate = this.FrameRate;
			profile.BitRate = this.BitRate;
			return profile;
		} catch (Exception e) {
			throw RuntimeExceptionEx.Create(e);
		}
	}

	public static CamVideoCoderProfile GetSystemDefault_CamVideoCoderProfile() {
		int backCam = CameraHelper.FindBackCamera();
		if (backCam != -1)
			return GetSystemDefault_CamVideoCoderProfile(backCam);

		int frontCam = CameraHelper.FindFrontCamera();
		if (frontCam != -1)
			return GetSystemDefault_CamVideoCoderProfile(frontCam);
		return null;
	}

	public static CamVideoCoderProfile GetSystemDefault_CamVideoCoderProfile(int hardwareCameraId) {
		CamVideoCoderProfile profile = new CamVideoCoderProfile();

		profile.HardwareCameraId = hardwareCameraId;
		profile.IsSystemDefault = true;
		profile.IsHardwareDefault = false;
		profile.VideoSize = GetSupportDefaultResolution(hardwareCameraId);
		profile.FrameRate = 15;
		profile.BitRate = profile.VideoSize.getWidth() * profile.VideoSize.getHeight() * 4;
		return profile;
	}

	public static CamVideoCoderProfile GetSystemDefault_CamVideoCoderProfile(CameraType ct) {
		int hcid = ct.toHardwareCameraId();
		if (hcid == -1)
			return null;
		else {
			return GetSystemDefault_CamVideoCoderProfile(hcid);
		}
	}

	public static CamVideoCoderProfile GetHardwareDefault_CamVideoCoderProfile() {
		int backCam = CameraHelper.FindBackCamera();
		if (backCam != -1)
			return GetSystemDefault_CamVideoCoderProfile(backCam);

		int frontCam = CameraHelper.FindFrontCamera();
		if (frontCam != -1)
			return GetSystemDefault_CamVideoCoderProfile(frontCam);
		return null;
	}

	public static CamVideoCoderProfile GetHardwareDefault_CamVideoCoderProfile(int hardwareCameraId) {
		CamVideoCoderProfile profile = new CamVideoCoderProfile();
		CamcorderProfile cp = android.media.CamcorderProfile.get(hardwareCameraId, CamcorderProfile.QUALITY_QVGA);
		profile.HardwareCameraId = hardwareCameraId;
		if (cp == null) {
			profile.IsSystemDefault = true;
			profile.IsHardwareDefault = false;
			profile.VideoSize = ResolutionType.GetDefault();
			profile.FrameRate = 15;
			profile.BitRate = profile.VideoSize.getWidth() * profile.VideoSize.getHeight() * 4;
			// profile.BitRate = 1000 * 160;
		} else {
			profile.IsSystemDefault = false;
			profile.IsHardwareDefault = true;
			profile.VideoSize = GetSupportDefaultResolution(hardwareCameraId);
			profile.setVideoSize(cp.videoFrameWidth, cp.videoFrameHeight);
			profile.BitRate = cp.videoBitRate;
			profile.FrameRate = cp.videoFrameRate;
		}

		return profile;
	}

	public static CamVideoCoderProfile GetHardwareDefault_CamVideoCoderProfile(CameraType ct) {
		int hcid = ct.toHardwareCameraId();
		if (hcid == -1)
			return null;
		else {
			return GetHardwareDefault_CamVideoCoderProfile(hcid);
		}
	}

	private static ResolutionType GetSupportDefaultResolution(int hardwareCameraId) {
		ListEx<ResolutionType> list = GetSupportResolution(hardwareCameraId);
		
		for (ResolutionType type : list)
			if (type == ResolutionType.VGA)
				return type;
		
		for (ResolutionType type : list)
			if (type == ResolutionType.QVGA)
				return type;

		for (ResolutionType type : list)
			if (type == ResolutionType.CIF)
				return type;

		for (ResolutionType type : list)
			if (type == ResolutionType.D1)
				return type;

		for (ResolutionType type : list)
			if (type == ResolutionType._480P)
				return type;

		for (ResolutionType type : list)
			if (type == ResolutionType.QCIF)
				return type;

		return ResolutionType.VGA;
	}

	private static CamcorderProfile GetCamcorderProfile(int hardwareCameraId, int code) {
		try {
			return android.media.CamcorderProfile.get(hardwareCameraId, code);
		} catch (Throwable e) {
			return null;
		}
	}

	private static ListEx<ResolutionType> GetSupportResolution(int hardwareCameraId) {
 
		CamcorderProfile cpQVGA = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_QVGA);
		CamcorderProfile cpQCIF = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_QCIF);
		CamcorderProfile cpCIF = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_CIF);
		CamcorderProfile cpHIGH = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_HIGH);
		CamcorderProfile cpLOW = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_LOW);
		CamcorderProfile cp480P = GetCamcorderProfile(hardwareCameraId, CamcorderProfile.QUALITY_480P);
		ListEx<ResolutionType> list = new ListEx<ResolutionType>();
		if (cpQVGA != null)
			list.add(ResolutionType.QVGA);
		if (cpQCIF != null)
			list.add(ResolutionType.QCIF);
		if (cpCIF != null)
			list.add(ResolutionType.CIF);
		if (cp480P != null)
			list.add(ResolutionType._480P);
		if (cpLOW != null) {
			for (ResolutionType type : ResolutionType.values()) {
				if (type.getWidth() == cpLOW.videoFrameWidth && type.getHeight() == cpLOW.videoFrameHeight) {
					list.add(type);
				}
			}
		}
		if (cpHIGH != null) {
			for (ResolutionType type : ResolutionType.values()) {
				if (type.getWidth() == cpHIGH.videoFrameWidth && type.getHeight() == cpHIGH.videoFrameHeight) {
					list.add(type);
				}
			}
		}
		return list;
	}

	// 分辨率
	public static enum ResolutionType {
		QCIF((int) 0x1), CIF((int) 0x2), D1((int) 0x3), QVGA((int) 0x4), VGA((int) 0x5), SVGA((int) 0x6), _480P((int) 0x16), _720P((int) 0x17), _1080P((int) 0x18);
		private int id;

		private ResolutionType(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static ResolutionType forId(int id) {
			for (ResolutionType type : ResolutionType.values())
				if (type.getId() == id)
					return type;
			throw new RuntimeException();

		}

		public static ResolutionType GetDefault() {
			return QVGA;
		}

		private int[] getSize() {
			int width = 0;
			int height = 0;
			ResolutionType type = this;
			if (ResolutionType.QCIF == type) {
				width = 176;
				height = 144;
			} else if (ResolutionType.CIF == type) {
				width = 352;
				height = 288;
			} else if (ResolutionType.D1 == type) {
				width = 704;
				height = 576;
			} else if (ResolutionType.QVGA == type) {
				width = 320;
				height = 240;
			} else if (ResolutionType.VGA == type) {
				width = 640;
				height = 480;
			} else if (ResolutionType.SVGA == type) {
				width = 800;
				height = 600;
			} else if (ResolutionType._480P == type) {
				width = 720;
				height = 480;
			} else if (ResolutionType._720P == type) {
				width = 1280;
				height = 720;
			} else if (ResolutionType._1080P == type) {
				width = 1920;
				height = 1080;
			} else {
				width = 320;
				height = 240;
			}
			return new int[] { width, height };
		}

		public int getWidth() {
			return getSize()[0];
		}

		public int getHeight() {
			return getSize()[1];
		}
	}

	public static enum CameraType {
		BackCamera((int) 0x0), FrontCamera((int) 0x1);
		private int id;

		private CameraType(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public int toHardwareCameraId() {
			if (this == CameraType.BackCamera)
				return CameraHelper.FindBackCamera();
			if (this == CameraType.FrontCamera)
				return CameraHelper.FindFrontCamera();
			return -1;
		}

		public boolean exist() {
			return toHardwareCameraId() != -1;
		}

		public static CameraType getDefault() {

			if (CameraHelper.FindBackCamera() != -1)
				return CameraType.BackCamera;

			if (CameraHelper.FindFrontCamera() != -1)
				return CameraType.FrontCamera;

			throw new RuntimeExceptionEx("未找到摄像机");
		}

	}
}
