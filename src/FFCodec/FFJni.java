package FFCodec;

import yangTalkback.Codec.FFCodec.AVCodecCfg;
import yangTalkback.Codec.FFCodec.FFCode;

import AXLib.Utility.*;

//FFMPEG封装
public class FFJni {
	public static int libType = 0;
	private static boolean libLoaded = false;

	// 初始化
	public static native boolean init();

	// 初始化编解码器
	public static native int codecInit(byte[] cfgBuff, int type);//
	// 视频解码

	public static native int videoDecode(int pAVObj, byte[] inFBuff, byte[] outFDBuff);

	// 视频编码
	public static native int videoEncode(int pAVObj, byte[] inFBuff, byte[] outFDBuff);

	// 要解码第一帧成功后才能调
	public static native int videoGetWidth(int pAVObj);

	// 要解码第一帧成功后才能调
	public static native int videoGetHeight(int pAVObj);

	// public static native int audioDecode(JNIEnv* env,jobject thiz,AVObj
	// obj,jbyteArray jinFDBuff,jbyteArray joutFDBuff);

	// public static native int audioEncode(JNIEnv* env,jobject thiz,AVObj
	// obj,jbyteArray jinFDBuff,jbyteArray joutFDBuff);

	public static void Test() throws Exception {
		FFJni.loadLib();
		FFJni.init();
		AVCodecCfg cfg = AVCodecCfg.CreateVideo(704, 576, FFCode.CODEC_ID_H264, 96000);
		byte[] cfgBuff = cfg.getBytes();
		int pDecAVObj = FFJni.codecInit(cfgBuff, 1);
		int pEncAVObj = FFJni.codecInit(cfgBuff, 2);

		java.io.FileInputStream is = new java.io.FileInputStream("c:\\test.h264");
		LittleEndianDataInputStream dataStream = new LittleEndianDataInputStream(is);
		int id = 0;
		byte[] outFDBuff = new byte[cfg.width * cfg.height * 3];
		while (is.available() > 0) {

			int len = dataStream.readInt();
			byte[] inFBuf = dataStream.readFully(len);
			int r = FFJni.videoDecode(pDecAVObj, inFBuf, outFDBuff);
			System.out.printf("dec:r:%d index:%d\n ", r, id++);

			// byte[] outEncFDBuff=new byte[outFDBuff.length];
			// r =FFJni.videoEncode(pEncAVObj, outFDBuff, outEncFDBuff);
			// System.out.printf("enc:r:%d index:%d\n ",r, id);

			if (is.available() == 0) {
				is.close();
				is = new java.io.FileInputStream("c:\\test.h264");
				dataStream = new LittleEndianDataInputStream(is);
			}

		}

	}

	// 加载类库
	public static void loadLib() {
		if (libLoaded)
			return;
		try {
			if (libType == 0) {
				System.loadLibrary("ffmpeg");
				System.loadLibrary("ffjni");

			} else {
				System.loadLibrary("CJJ");
			}
		} catch (Throwable e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		libLoaded = true;
	}
}
