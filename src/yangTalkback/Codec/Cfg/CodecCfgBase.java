package yangTalkback.Codec.Cfg;

import AXLib.Utility.BitConverter;
import AXLib.Utility.RuntimeExceptionEx;

import android.media.MediaRecorder;

//�������û���
public class CodecCfgBase {
	public int encoder = -1;// ͨ�ñ������
	public String encodeName = null;// ͨ�ñ�������
	// ���ñ�������

	public void SetEncoder(String name) {
		name = name.toUpperCase();
		encodeName = name;
		encoder = GetGeneralEncoder(name);
	}

	// ��ȡandroidϵͳ�϶�Ӧ�ı������
	public int GetAndroidEncoder(int generalEncoder) {
		String name = GetGeneralEncodecName(generalEncoder);
		return GetAndroidEncoder(name);
	}

	// ��ȡandroidϵͳ�϶�Ӧ�ı������
	public static int GetAndroidEncoder(String name) {
		name = name.toUpperCase();
		if (name.equalsIgnoreCase("H264"))
			return MediaRecorder.VideoEncoder.H264;
		if (name.equalsIgnoreCase("H263"))
			return MediaRecorder.VideoEncoder.H263;
		if (name.equalsIgnoreCase("PCM"))
			return MediaRecorder.AudioEncoder.DEFAULT;
		throw new RuntimeExceptionEx("δ��ƥ���������");
	}
	//��ȡͨ�ñ������
	public static int GetGeneralEncoder(String name) {
		byte[] buf = BitConverter.GetBytes(name);
		return BitConverter.ToInt(buf);
	}
	//��ȡͨ�õı�������
	public static String GetGeneralEncodecName(int generalEncoder) {
		byte[] buf = BitConverter.GetBytes(generalEncoder);
		return BitConverter.ToString(buf);
	}
}