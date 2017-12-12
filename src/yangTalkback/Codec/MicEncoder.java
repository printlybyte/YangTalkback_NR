package yangTalkback.Codec;

import com.ryong21.encode.Speex;

import AXLib.Utility.CallBack;
import AXLib.Utility.Event;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;

import yangTalkback.App.App;
import yangTalkback.Codec.Cfg.AudioEncodeCfg;
import yangTalkback.Comm.*;
import yangTalkback.Media.MediaFrame;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

//麦克风采集编码器
public class MicEncoder {
	public final Event<MediaFrame> Encoded = new Event<MediaFrame>();// 编码一帧完成事件

	private AudioEncodeCfg audioCfg = null;// 编码设置
	private Thread encThread = null;// 编码线程
	private Thread pcmThread = null;// 采集线程
	private boolean isRuning = false;// 运行状态
	private AudioRecord recordInstance;// 音频录制对象
	private MicEncoderDataReceiver receiver;// 音频接收器
	private Queue<short[]> pcmQueue = new Queue<short[]>();// 采集数据队列
	private Speex speex = null;// 编码对象
	private boolean eachACSeted = false;// 是否设置回音消除
	private int chl = -1;// 通道
	private int fmt = -1;// 位元
	private int freq = -1;// 采样
	private int minBufferSize = -1;// 缓冲大小
	private String _playSyncKey = null;// 回音消除同步标识,没有用到

	public MicEncoder(AudioEncodeCfg cfg, MicEncoderDataReceiver receiver) {
		audioCfg = cfg;
		this.receiver = receiver;
		freq = cfg.frequency;
		chl = cfg.channel == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		fmt = cfg.format == 0 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;

	}

	public void start() {
		if (isRuning)
			throw new IllegalStateException("encoder is running");
		isRuning = true;

		pcmThread = ThreadEx.GetThreadHandle(new CallBack(this, "ReadPCMThread"), "PCM音频采集线程");
		encThread = ThreadEx.GetThreadHandle(new CallBack(this, "EncodeThread"), "音频编码线程");

		pcmThread.start();
		encThread.start();
	}

	public void stop() {
		if (!isRuning)
			return;
		try {
			isRuning = false;
			ThreadEx.stop(encThread);
			ThreadEx.stop(pcmThread);
			ReleaseAudioRecord();
			pcmThread = null;
			encThread = null;
			pcmQueue.clear();
		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
	}

	// 编码线程
	public void EncodeThread() {
		if (speex == null) {
			speex = new Speex(this.audioCfg.compression);

		}
		byte[] tempBuffer = new byte[1024];
		int indexByRate = 0;

		while (this.isRuning) {
			if (pcmQueue.size() > 0) {
				short[] pcm = pcmQueue.remove();
				int size = speex.encode(speex.pSpx, pcm, 0, tempBuffer, pcm.length);
				// int size = speex.encode(pcm, 0, tempBuffer, pcm.length);
				if (size == 0)// 为0时表示静音状态
					continue;
				byte[] data = new byte[size];
				System.arraycopy(tempBuffer, 0, data, 0, size);
				MediaFrame frame = null;

				if (indexByRate % audioCfg.keyFrameRate == 0) {
					indexByRate = 0;
					frame = MediaFrame.CreateAudioKeyFrame(this.audioCfg, getCurrentTimeMillis(), data, 0, size);
				} else {
					frame = MediaFrame.CreateAudioFrame(this.audioCfg, getCurrentTimeMillis(), data, 0, size);
				}
				indexByRate++;
				onEncoded(frame);
			} else {
				App.SleepOrWait(null);
			}
		}

	}

	void calc1(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			lin[i + off] = (short) (j >> 2);
		}
	}

	// 音频采集线程
	public void ReadPCMThread() {
		try {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			int bufferSize = AudioRecord.getMinBufferSize(freq, chl, fmt);
			InitAudioRecord();
			short[] tempBuffer = new short[bufferSize];
			pcmQueue.clear();
			while (isRuning) {

				int size = 0;
				if (_playSyncKey == null) {
					// 没有经过回音消除处理
					size = recordInstance.read(tempBuffer, 0, audioCfg.samples);
					if (size > 0) {
						short[] data = new short[size];
						System.arraycopy(tempBuffer, 0, data, 0, size);
						//calc1(data, 0, size);
						pcmQueue.add(data);
					}
				} else {
					// 以下经过回音消除处理
					size = SpeexEchoAC.Record(_playSyncKey, recordInstance, tempBuffer, audioCfg.samples);
					if (size > 0) {
						short[] data = new short[size];
						System.arraycopy(tempBuffer, 0, data, 0, size);
						pcmQueue.add(data);
					}
				}
				// App.SleepOrWait(null);
			}
		} catch (Exception e) {
			CLLog.Error(e);
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			if (isRuning) {
				throw new RuntimeExceptionEx(e);
			}
		}

	}

	// 初始化音频采集对象
	private void InitAudioRecord() {
		if (recordInstance != null)
			ReleaseAudioRecord();
		this.minBufferSize = AudioRecord.getMinBufferSize(freq, chl, fmt);
		recordInstance = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, freq, chl, fmt, minBufferSize);

		try {
			recordInstance.startRecording();
		} catch (Exception e) {
			ThreadEx.sleep(500);
			recordInstance.startRecording();
		}

	}

	// 释放音频采集对象
	private void ReleaseAudioRecord() {
		try {
			if (recordInstance != null) {
				recordInstance.stop();
			}
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
		try {
			if (recordInstance != null) {
				recordInstance.release();
			}
			recordInstance = null;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}

	}

	public void SetAudioSyncKey(String key) {
		_playSyncKey = key;
		SetEchoACSpeex();

	}

	private void SetEchoACSpeex() {
		if (eachACSeted)
			return;
		if (_playSyncKey != null && speex != null) {
			boolean success = SpeexEchoAC.SetSpeex(_playSyncKey, speex);
			eachACSeted = success;
		}
	}

	// 引发编码完成事件
	protected void onEncoded(MediaFrame mf) {
		if (receiver != null)
			receiver.Received(mf);
		if (Encoded.getHandleCount() > 0)
			Encoded.Trigger(this, mf);
	}

	public static interface MicEncoderDataReceiver {
		void Received(MediaFrame frame);
	}

	protected long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}
}
