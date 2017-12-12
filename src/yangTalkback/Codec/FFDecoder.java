package yangTalkback.Codec;

import java.nio.ByteBuffer;

import yangTalkback.Codec.FFCodec.AVCodecCfg;
import yangTalkback.Codec.FFCodec.FFCode;
import yangTalkback.Codec.FFCodec.FFObj;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.VideoDisplayFrame;
import android.graphics.Bitmap;

import AXLib.Utility.IDisposable;

//FFMPEG 解码器
public class FFDecoder implements IDisposable {
	FFObj ffObj = null;
	AVCodecCfg cfg = null;
	boolean inited = false;
	private int _width = 0;
	private int _height = 0;

	private int decodedFrame[];
	// 图片像素存放区域
	private byte[] mPixel = null;
	// 图片存放内存区间
	private ByteBuffer bmpBuffer = null;
	// 解码后的图片
	private Bitmap VideoBit = null;
	private FFCode _ffCode;

	// 解码一桢图片的事件
	// public final Event<Bitmap> Decoded = new Event<Bitmap>();

	public FFDecoder(FFCode code) {
		_ffCode = code;
	}

	// 初始化
	private void Init(MediaFrame mf) throws Exception {
 
	}

	// 初始化
	private boolean tryResetSize(MediaFrame mf) throws Exception {
		return false;
	}

	public VideoDisplayFrame Deocde(MediaFrame mf) throws Exception {
		 
		return null;
	}

	// // 引发解码完成事件
	// private void OnDecoded(Bitmap bitmap) {
	// if (Decoded.getHandleCount() >= 0)
	// Decoded.Trigger(this, bitmap);
	// }

	@Override
	public void Dispose() {
		if (ffObj != null)
			ffObj.Dispose();
	}
}
