package yangTalkback.Codec;

import java.util.HashMap;

import com.ryong21.encode.Speex;

import android.media.AudioRecord;
import android.media.AudioTrack;

import AXLib.Utility.Queue;

//回音消除,没有用到
public class SpeexEchoAC {

	private static HashMap<String, Speex> _Speexs = new HashMap<String, Speex>();
	public static boolean EnabledEC = true;
	public static boolean IsTest = true;

	public static String Allow() {
		String key = AXLib.Utility.Ex.FunEx.GetGUIDString();
		_Speexs.put(key, null);
		return key;
	}

	public static void Release(String key) {
		if (key == null)
			return;
		if (_Speexs.containsKey(key)) {
			_Speexs.remove(key);
		}
	}

	public static boolean SetSpeex(String key, Speex speex) {

		if (key != null && speex != null && _Speexs.containsKey(key)) {
			_Speexs.put(key, speex);
			return true;
		}
		return false;
	}

	public static boolean EchoPlayback(String key, short[] data) {
		if (key != null && _Speexs.containsKey(key)) {
			Speex speex = _Speexs.get(key);
			if (speex != null) {
				speex.EchoPlayback(speex.pSpx, data);
				return true;
			}
		}
		return false;
	}

	public static boolean EchoCapture(String key, short mic[], short revert[]) {
		if (key != null && _Speexs.containsKey(key)) {
			Speex speex = _Speexs.get(key);
			if (speex != null) {
				speex.EchoCapture(speex.pSpx, mic, revert);
				return true;
			}
		}
		return false;
	}

	static Queue<short[]> m_cap_q = new Queue<short[]>();
	static Queue<short[]> m_play_q = new Queue<short[]>();
	static Queue<short[]> m_out_play = new Queue<short[]>();
	static Queue<short[]> m_out_cap = new Queue<short[]>();
	static AudioTrack _track = null;
	static AudioRecord _record = null;
	static Speex _speex = new Speex();
	static int framesize = 160;
	static Boolean _started = false;

	public static short[] echo_capture(short[] capture) {
		short[] buffer = new short[framesize];
		_speex.EchoCapture(_speex.pSpx, capture, buffer);

		return buffer;
	}

	public static void echo_playback(short[] play) {
		_speex.EchoPlayback(_speex.pSpx, play);

	}

	static Thread _threadThread = new Thread(new Runnable() {

		@Override
		public void run() {
			_speex.Denoise(_speex.pSpx, -25);
			_speex.VAD(_speex.pSpx, 80, 65);
			// _speex
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			m_cap_q.clear();
			m_play_q.clear();
			while (true) {
				// if(m_cap_q.size()>0){
				// short[] buffer = new short[framesize];
				// m_out_cap.add(echo_capture(m_cap_q.remove()));
				//
				// }
				if (m_play_q.size() > 0) {
					short[] data = m_play_q.remove();
					echo_playback(data);
					int result = _track.write(data, 0, data.length);
				}
				short[] cap = new short[framesize];
				_record.read(cap, 0, framesize);
				calc2(cap, 0, cap.length);
				m_out_cap.add(echo_capture(cap));

			}
		}
	});
	static Thread _threadThread1 = new Thread(new Runnable() {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			while (true) {
				if (_record != null) {
					int size = 160;
					short[] data = new short[size];
					_record.read(data, 0, size);
					m_cap_q.add(data);
				}
			}
		}
	});

	public static int Play(String key, AudioTrack track, short[] data, int size) {
		_track = track;
		m_play_q.add(data);
		if (_track != null && _record != null && !_started) {
			_started = true;
			_threadThread.start();
			// _threadThread1.start();
		}
		// if(m_out_play.size()>0){
		// data=m_out_play.removeFirst();
		// int result = track.write(data, 0, size);
		// return result;
		// }
		return 0;
	}

	public static int Record(String key, AudioRecord record, short[] data, int size) {
		_record = record;
		if (m_out_cap.size() > 0) {
			short[] mic = m_out_cap.remove();
			System.arraycopy(mic, 0, data, 0, mic.length);
			return mic.length;
		}
		return 0;
	}

	public static int Play1(String key, AudioTrack track, short[] data, int size) {
		int result = 0;
		if (key != null && _Speexs.containsKey(key) && EnabledEC) {
			Speex speex = _Speexs.get(key);
			if (IsTest) {
				synchronized (speex) {
					speex.EchoPlayback(speex.pSpx, data);
				}
			} else
				speex.EchoPlayback(speex.pSpx, data);
			result = track.write(data, 0, size);
		} else {
			result = track.write(data, 0, size);
		}
		return result;
	}

	public static int Record1(String key, AudioRecord record, short[] data, int size) {
		int result = 0;
		if (key != null && _Speexs.containsKey(key) && EnabledEC) {
			Speex speex = _Speexs.get(key);

			result = record.read(data, 0, size);
			short[] mic = new short[result];
			System.arraycopy(data, 0, mic, 0, result);
			if (IsTest)
				synchronized (speex) {
					speex.EchoCapture(speex.pSpx, mic, data);
				}
			else
				speex.EchoCapture(speex.pSpx, mic, data);

		} else {
			result = record.read(data, 0, size);
			if (!IsTest)
				calc1(data, 0, size);
		}
		return result;
	}

	private static void calc1(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			lin[i + off] = (short) (j >> 2);
		}
	}

	// decline the volume
	static void calc2(short[] lin, int off, int len) {
		int i, j;

		for (i = 0; i < len; i++) {
			j = lin[i + off];
			if (j > 16350)
				lin[i + off] = 16350 << 1;
			else if (j < -16350)
				lin[i + off] = -16350 << 1;
			else
				lin[i + off] = (short) (j << 1);
		}
	}
}
