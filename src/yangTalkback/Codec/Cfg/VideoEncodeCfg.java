package yangTalkback.Codec.Cfg;

import yangTalkback.App.AppConfig;
import yangTalkback.Codec.CamVideoCoderProfile;
import yangTalkback.Media.MediaFrame;

import android.view.Surface;
import android.view.SurfaceHolder;

public class VideoEncodeCfg extends Mp4Cfg {
	public static int BACK_CAMERA = 0;
	public static int FRONT_CAMERA = 1;
	public int Orientation = 0;
	public Surface surface = null;
	public SurfaceHolder holder = null;

	public static VideoEncodeCfg getDefaule(SurfaceHolder holder) {
		CamVideoCoderProfile profile = AppConfig.Instance.GetCamVideoCoderProfile();
		return getDefaule(profile, holder);
	}

 

	public static VideoEncodeCfg Create(MediaFrame f) {
		VideoEncodeCfg cfg = new VideoEncodeCfg();
		cfg.SetEncoder(VideoEncodeCfg.GetGeneralEncodecName(f.nEncoder));
		cfg.width = f.nWidth;
		cfg.height = f.nHeight;
		return cfg;

	}
 
	public static VideoEncodeCfg getDefaule(CamVideoCoderProfile profile, SurfaceHolder holder) {
		VideoEncodeCfg encCfg = new VideoEncodeCfg();
		if (AppConfig.Instance.VideoEncodeMode == 0)
			encCfg.SetEncoder("H264");
		else if (AppConfig.Instance.VideoEncodeMode == 1)
			encCfg.SetEncoder("JPEG");
		encCfg.surface = holder.getSurface();
		encCfg.holder=holder;
		encCfg.videoBitRate = profile.BitRate;
		encCfg.frameRate = profile.FrameRate;
		encCfg.height = profile.VideoSize.getHeight();
		encCfg.width = profile.VideoSize.getWidth();
		encCfg.cameraId = profile.HardwareCameraId;
		return encCfg;
	}

}
