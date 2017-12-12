package yangTalkback.Codec.FFCodec;

import AXLib.Utility.IDisposable;
import AXLib.Utility.LittleEndianDataOutputStream;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.TH;
import FFCodec.FFJni;
//FFMPEG中的操作对象
public class FFObj implements IDisposable {
	final int AVCODEC_MAX_AUDIO_FRAME_SIZE = 192000;
	private FFCodecType _codecType;
	private int _pAVObj = -1;
	private byte[] _outFDBuff = null;

	public FFObj(FFCodecType codecType) {
		_codecType = codecType;
	}
	//初始化
	public void init(AVCodecCfg cfg) throws Exception {

		FFJni.loadLib();

		FFJni.init();
		byte[] cfgBuff = cfg.getBytes();
		_pAVObj = FFJni.codecInit(cfgBuff, _codecType.getId());
		if (_pAVObj < 0)
			throw new Exception("初始化失败");
		if (_codecType == FFCodecType.VideoDecode || _codecType == FFCodecType.VideoEncode)
			_outFDBuff = new byte[cfg.width * cfg.height * 3 + (new DFrame()).getSize()];
		if (_codecType == FFCodecType.AudioDecode || _codecType == FFCodecType.AudioEncode)
			_outFDBuff = new byte[AVCODEC_MAX_AUDIO_FRAME_SIZE];

	}
//编码解码
	public DFrame code(byte[] buff) throws Exception {
		if (_pAVObj == -1)
			throw new Exception("未初始化或初始化失败");
		int r = -1;
		try {
			if (_codecType == FFCodecType.VideoDecode)
				r = videoDecode(buff);
			else if (_codecType == FFCodecType.VideoEncode)
				r = videoEncode(buff);
		} catch (Throwable e) {
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		if (r == 1) {
			DFrame df = new DFrame();
			df.setBytes(_outFDBuff);
			// Console.d("DFrame", "key:" + df.nIsKeyFrame + "   DFrame:" +
			// df.nSize);
			byte[] outBuff = new byte[df.nSize];
			System.arraycopy(_outFDBuff, df.getSize(), outBuff, 0, df.nSize);
			df.Data = outBuff;

			// wf(df,outBuff);
			return df;
		}
		return null;
	}

	private int videoDecode(byte[] inFBuff) {
		return FFJni.videoDecode(_pAVObj, inFBuff, _outFDBuff);
	}

	private int videoEncode(byte[] inFBuff) {
		return FFJni.videoEncode(_pAVObj, inFBuff, _outFDBuff);
	}
	public int[] tryGetVideoSize() {
		return new int[] { FFJni.videoGetWidth(_pAVObj), FFJni.videoGetHeight(_pAVObj) };

	}

	java.io.FileOutputStream fo = null;
	LittleEndianDataOutputStream os = null;

	private void wf(DFrame df, byte[] buff) {
		try {
			if (fo == null) {

				fo = new java.io.FileOutputStream("/sdcard/dcim/tt1.h264", false);
				os = new LittleEndianDataOutputStream(fo);
			}
			os.write(buff, 0, buff.length);
			os.flush();
		} catch (Exception e) {
			TH.Throw(e);
		}
		// "/sdcard/dcim/camera/tt1.h264"

	}

	@Override
	public void Dispose() {
		// assert false : "未实现Dispose";
		// throw new RuntimeException("未实现Dispose");

	}
}
