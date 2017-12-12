package yangTalkback.Codec;

import java.nio.ByteBuffer;

import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.VideoDisplayFrame;

import android.graphics.Bitmap;

import h264.com.VView;
import AXLib.Utility.Event;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;

//H264解码器,没有用到
public class H264AndroidDecoder extends VView implements IDisposable {

	// 图片像素存放区域
	protected byte[] mPixel = null;
	// 图片存放内存区间
	protected ByteBuffer bmpBuffer = null;
	// 解码后的图片
	protected Bitmap VideoBit = null;

	// 视频数据接收缓冲区
	protected Queue<MediaFrame> qFrame = null;
	// 标识解码线程是否正在工作中
	protected boolean decodeThreadWorking = false;
	// 最大缓冲时间
	protected final int MaxBufferTime = 1;

	// 解码一桢图片的事件
	// public final Event<Bitmap> Decoded = new Event<Bitmap>();

	public final Event<Exception> Error = new Event<Exception>();

	protected boolean inited = false;

	// 初始化
	private void Init(MediaFrame mf) {
		 
	}

	public VideoDisplayFrame Deocde(MediaFrame mf) throws Exception {
		 
		return null;
	}

 
	@Override
	public void Dispose() {
	 
	}
}
