package yangTalkback.Codec;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Semaphore;

import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import Tools.*;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Media.MediaFrame;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Environment;

//摄像头采集编码器
@SuppressLint("NewApi")
public class CameraEncoder extends CameraEncoderBase implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, IDisposable {
	// 保存的测试文件
	private final String TESTFILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/axvchat_test.mp4";
	private static int _openCount = 0;// 打开次数
	private Date _lastStartTime;// 最后一次重置时间
	private int _restartSpan = 10;// 重置间隔分钟
	private boolean _needRestart = false;// 是否需要重置
	private boolean _restarting = false;// 是否正在重置中
	private boolean _isworking = false;// 当前是否在采集中
	private MediaRecorder mediaRec = null;// 采集器
	private InputStream fisVideoRead = null;// 采集数据读取流
	private DataInputStream disVideoRead = null;// 采集数据读取流
	private LSS lss = null;// 采集数据读取流
	private Tools.MP4Config mp4Config;// 编码参数
	private Semaphore lock = new Semaphore(0);// 同步信号量
	private boolean mMediaRecRecording;// 当前是否正在录制中
	private Thread receThread = null;// 采集数据接收线程
	private Thread _pushThread = null;// 采集数据推送线程
	private int _frameIndex = 0;// 帧序
	private boolean _pushMode = false;// 推送模式
	protected CameraEncoderDataReceiver receiver = null;// 采集数据接收器
	private byte[] h264head = new byte[] { 0, 0, 0, 1 };// H264 帧分隔
	private byte[] spspps = null;// H264 SPS PPS
	private byte[] h263head = new byte[] { 0, 0, 80 };// H263帧头没有用到
	private final static int h263FrameMaxSize = 1024 * 64;// H263帧大小,没有用到
	private Queue<MediaFrame> _pushQueue = new Queue<MediaFrame>();// 帧推送队列
	public boolean Stoped = false;// 是否已经停止

	public CameraEncoder(VideoEncodeCfg cfg, int restartSpan, CameraEncoderDataReceiver receiver) {
		encCfg = cfg;
		this.receiver = receiver;
		_restartSpan = restartSpan;
		_needRestart = restartSpan > 0;

		_pushMode = android.os.Build.VERSION.SDK_INT > 15;
	}

	public void start() {
		 

	}

	public void stop() {
		if (!_isworking)
			return;
		_isworking = false;

		try {
			releaseMediaRecorder();
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
		try {
			mMediaRecRecording = false;
			ThreadEx.stop(receThread);
			ThreadEx.stop(_pushThread);
			receThread = null;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}

		if (lss != null)
			lss.Dispose();
		lss = null;
		Stoped = true;
	}

	// 初始化,当ISTESE=TRUE时,要尝试性播放主要是为了获取SPS PPS
	private boolean initMediaRec(boolean isTest) throws Exception {
		 
		return true;
	}

	private void releaseMediaRecorder() {
		releaseMediaRecorder(0);
	}

	// 以下各个设备厂商实现的接口方式有很多不同，所以增加了很多错误处理
	private void releaseMediaRecorder(int sleep) {
		 
	}

	// 读取采集数据线程
	public void ReceiveThread() throws Exception {
		 
	}

	// 推送线程
	public void PushThread() {

		 
	}

	// 需要优化
	private MediaFrame DecodeH264(byte[] data) throws Exception {
		return null;
	}

	private void Skipmdat(DataInputStream dis) throws IOException {
		 
	}

	protected void onEncoded(MediaFrame mf) {
		 
	}

	// 重置采集器
	public void Restart() {
		 

	}

	// 定焦
	public void AutoFocus() {
		if (camera != null) {
			camera.autoFocus(null);
		}
	}

	@Override
	public void Dispose() {
		if (lss != null)
			lss.Dispose();
	}

	// 采集数据接收器
	public static interface CameraEncoderDataReceiver {
		void Received(MediaFrame frame);
	}

}
