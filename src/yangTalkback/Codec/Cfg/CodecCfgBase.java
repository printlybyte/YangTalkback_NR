package yangTalkback.Codec.Cfg;

import AXLib.Utility.BitConverter;
import AXLib.Utility.RuntimeExceptionEx;

import android.media.MediaRecorder;

//编码配置基类
public class CodecCfgBase {
	public int encoder = -1;// 通用编码代号
	public String encodeName = null;// 通用编码名称
	// 设置编码名称

	public void SetEncoder(String name) {
		name = name.toUpperCase();
		encodeName = name;
		encoder = GetGeneralEncoder(name);
	}

	// 获取android系统上对应的编码代号
	public int GetAndroidEncoder(int generalEncoder) {
		String name = GetGeneralEncodecName(generalEncoder);
		return GetAndroidEncoder(name);
	}

	// 获取android系统上对应的编码代号
	public static int GetAndroidEncoder(String name) {
		name = name.toUpperCase();
		if (name.equalsIgnoreCase("H264"))
			return MediaRecorder.VideoEncoder.H264;
		if (name.equalsIgnoreCase("H263"))
			return MediaRecorder.VideoEncoder.H263;
		if (name.equalsIgnoreCase("PCM"))
			return MediaRecorder.AudioEncoder.DEFAULT;
		throw new RuntimeExceptionEx("未能匹配编码名称");
	}
	//获取通用编码代号
	public static int GetGeneralEncoder(String name) {
		byte[] buf = BitConverter.GetBytes(name);
		return BitConverter.ToInt(buf);
	}
	//获取通用的编码名称
	public static String GetGeneralEncodecName(int generalEncoder) {
		byte[] buf = BitConverter.GetBytes(generalEncoder);
		return BitConverter.ToString(buf);
	}
}
