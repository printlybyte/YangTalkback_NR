package yangTalkback.Media;

import AXLib.Utility.CallBack;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import yangTalkback.App.App;
import yangTalkback.Codec.SpeexDecode;
import yangTalkback.Codec.SpeexEchoAC;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

//声音播放器
public class AudioPlay implements IDisposable {
	private AudioTrack track = null;// 声音播放对象
	private SpeexDecode decode = null;// 解码器
	private int _speakMode = 0;// 对讲模式
	private int chl = -1;// 通道
	private int fmt = -1;// 位元
	private int freq = -1;// 采样
	private int minBufferSize = -1;// 缓冲大小
	private float volume = 1f;// 音量
	protected boolean _working = false;// 当前对象是否正在工作
	protected boolean playing = false;// 是否正在播放
	private boolean _isPlay = true;// 是正播放声音
	private Thread playThread = null;// 播放线程
	protected Queue<MediaFrame> qFrames = new Queue<MediaFrame>();// 播放帧队列
	protected Queue<short[]> qDecQueue = new Queue<short[]>();// 解码音频队列
	private String _playSyncKey = null;// 回音消除同线标识,没有用到
	private AudioManager _am = null;// 音频播放管理器
	private boolean _isReal = false;// 是否实时
	private boolean _isFastPlay = false;// 是否快速播放，该值用于在调节网络波动时延时处理

	/*
	 * mode 0外响模式，1内置模式
	 */
	public AudioPlay(int speakMode, boolean isReal) {
		_speakMode = speakMode;
		_isReal = isReal;
	}

	public void Play(MediaFrame mf) {
		if (!_working)
			return;
		if (mf.nIsAudio != 1)
			throw RuntimeExceptionEx.Create("音频帧错误");

		int mode = _speakMode == 0 ? AudioManager.STREAM_MUSIC : (_isReal ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
		if (track == null) {
			freq = 8000;// 采样
			chl = AudioFormat.CHANNEL_OUT_MONO;// 通道数
			fmt = AudioFormat.ENCODING_PCM_16BIT;// 位率
			minBufferSize = AudioTrack.getMinBufferSize(freq, chl, fmt);
			track = new AudioTrack(mode, freq, chl, fmt, minBufferSize, AudioTrack.MODE_STREAM);

			track.setPlaybackRate(freq);
			decode = new SpeexDecode();
			playing = true;
			playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"));
			playThread.start();
			track.play();
			// track.setStereoVolume(volume, volume);// 设置当前音量大小

			// 这两句话的作用是打开设备扬声器
			_am = (AudioManager) App.FirstAct.getSystemService(Context.AUDIO_SERVICE);
			// _am.setMode(AudioManager.ROUTE_SPEAKER);

			if (_speakMode == 0)
				_am.setSpeakerphoneOn(true);
			else
				_am.setSpeakerphoneOn(false);

			if (_isReal && _speakMode != 0) {
				int v1 = _am.getStreamVolume(mode);
				int v2 = _am.getStreamMaxVolume(mode);
				_am.setStreamVolume(mode, 4, mode);
			}

		}
		if (track != null) {
			short[] data = decode.Deocde(mf);
			qDecQueue.add(data);
		}
	}

	public void PlayThread() throws Exception {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		while (_working && playing && playThread != null) {
			int size = qDecQueue.size();
			if (_isReal) {
				if (size > 500) {
					short[] data = qDecQueue.remove();
					continue;
				} else if (size > 200 && !_isFastPlay) {
					_isFastPlay = true;
					track.setPlaybackRate(freq + freq / 5);
				} else if (size > 100 && !_isFastPlay) {
					_isFastPlay = true;
					track.setPlaybackRate(freq + freq / 10);
				}
			}
			if (size > 0) {

				short[] data = qDecQueue.remove();
				if (_isPlay) {
					if (_playSyncKey != null)
						SpeexEchoAC.Play(_playSyncKey, track, data, data.length);
					else
						track.write(data, 0, data.length);

					int v = _am.getStreamVolume(AudioManager.STREAM_MUSIC);
					// track.setStereoVolume(v, v);// 设置当前音量大小
				}
			} else {
				_isFastPlay = false;
				track.setPlaybackRate(freq);
				ThreadEx.sleep(10);
			}
		}
		playThread = null;
		playing = false;
	}

	public void Start() {
		if (_working)
			return;
		_working = true;

	}

	public void Stop() {
		if (!_working)
			return;
		_working = false;
		try {
			if (playThread != null) {
				ThreadEx.waitStop(playThread, 50);
				playThread = null;
			}
			playing = false;
			qFrames.clear();
			qDecQueue.clear();
			if (track != null)
				track.stop();

		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void SetAudioSyncKey(String key) {
		_playSyncKey = key;
	}

	public void PlaySwitch(boolean status) {
		_isPlay = status;
	}

	@Override
	public void Dispose() {
		Stop();
		qDecQueue.clear();
		qFrames.clear();
		if (track != null)
			track.release();
		if (decode != null)
			decode.Dispose();
	}

}
