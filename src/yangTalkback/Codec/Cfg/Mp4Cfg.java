package yangTalkback.Codec.Cfg;

import AXLib.Utility.*;
import AXLib.Utility.Ex.H16Str;

import android.media.MediaRecorder;

//H264视频编码参数
public class Mp4Cfg extends CodecCfgBase {

	public int width;
	public int height;
	public int frameRate;
	public int cameraId = -1;// 0为后置摄像头，1为前置摄像头
	public int outputFormat = MediaRecorder.OutputFormat.THREE_GPP;
	// public int quality = 50;
	public int videoBitRate = 1024 * 640;
	public byte[] SPS;
	public byte[] PPS;
	public String profileLevel;
	public String strSPS;
	public String strPPS;

	// 判断是否相等
	public boolean TestEq(Mp4Cfg cfg) {
		boolean r = true;
		r &= this.width == cfg.width;
		r &= this.height == cfg.height;
		r &= this.frameRate == cfg.frameRate;
		r &= this.cameraId == cfg.cameraId;
		r &= this.encoder == cfg.encoder;
		return r;

	}

	// 获取H264的SPS PPS
	public byte[] getSPSPPSBytes() throws Exception {
		if (this.PPS == null || this.SPS == null || this.profileLevel == null)
			throw new Exception();
		java.io.ByteArrayOutputStream baoStream = new java.io.ByteArrayOutputStream();
		baoStream.write(new byte[] { 0, 0, 0, 1 });
		baoStream.write(SPS);
		baoStream.write(new byte[] { 0, 0, 0, 1 });
		baoStream.write(PPS);
		byte[] pps_sps = baoStream.toByteArray();
		return pps_sps;
	}

	// 获取缓存的SPS PPS
	public boolean tryFillSPSPPS() {
		if (Mp4CfgListSettings.IsExists(this)) {
			try {
				Mp4CfgListSettings.fillSPSPPS(this);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	// 保存配置
	public void SaveCfg() throws Exception {
		Mp4CfgListSettings.AddMp4Cfg(this);
	}

	// 加载SPS PPS
	public boolean loadSPSPPS(String fileName) {

		// Mp4CfgListSettings.AddSettingByFile(fileName, this);
		// tryFillSPSPPS();
		Tools.MP4Config mp4Config = null;
		try {
			mp4Config = new Tools.MP4Config(fileName);
		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
		this.PPS = mp4Config.getPPS();
		this.SPS = mp4Config.getSPS();
		this.strPPS = H16Str.To16Strs(this.PPS);
		this.strSPS = H16Str.To16Strs(this.SPS);
		this.profileLevel = mp4Config.getProfileLevel();
		Mp4CfgListSettings.AddMp4Cfg(this);
		return true;
	}

}
