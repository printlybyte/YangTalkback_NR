package yangTalkback.Codec;

import AXLib.Utility.Console;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.StringEx;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Codec.FFCodec.AVCodecCfg;
import yangTalkback.Codec.FFCodec.DFrame;
import yangTalkback.Codec.FFCodec.FFCode;
import yangTalkback.Codec.FFCodec.FFCodecType;
import yangTalkback.Codec.FFCodec.FFObj;

import android.annotation.SuppressLint;

//2014-05-03ÐÞ¸ÄºóÎ´²âÊÔ
@SuppressLint({ "NewApi", "NewApi" })
public class CameraEncoderFFMPEG extends CameraEncoderYUV {

	public CameraEncoderFFMPEG(VideoEncodeCfg cfg, int restartSpan, CameraEncoderDataReceiver receiver) {
		super(cfg, restartSpan, receiver);

	}

	protected byte[] compressImage(byte[] data) {
		byte[] yuv420p = new byte[data.length];
		YUV420SP2YUV420(data, yuv420p, _previewSize.width, _previewSize.height);
		if (_ffEnc == null) {
			int width = _previewSize.width;
			int height = _previewSize.height;
			FFCode ffCode = FFCode.CODEC_ID_H264;
			if (StringEx.equalsIgnoreCase(encCfg.encodeName, "H264"))
				ffCode = FFCode.CODEC_ID_H264;
			if (StringEx.equalsIgnoreCase(encCfg.encodeName, "FLV1"))
				ffCode = FFCode.CODEC_ID_FLV1;
			if (StringEx.equalsIgnoreCase(encCfg.encodeName, "H263"))
				ffCode = FFCode.CODEC_ID_H263;

			AVCodecCfg cfgEnc = AVCodecCfg.CreateVideo(width, height, ffCode, 2400);

			_ffEnc = new FFObj(FFCodecType.VideoEncode);
			try {
				_ffEnc.init(cfgEnc);
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);
			}

		}
		DFrame df;
		try {
			df = _ffEnc.code(yuv420p);
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		if (df != null) {
			Console.d("CameraEncoderYUV", String.format("size:%d   key:%d", df.nSize, df.nIsKeyFrame));
			return df.Data;
		} else {
			Console.d("CameraEncoderYUV", "NULL");
			return null;
		}

	}

	public void Restart() {

	}

}