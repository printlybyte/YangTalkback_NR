package yangTalkback.Media;

import AXLib.Utility.CallBack;
import AXLib.Utility.ThreadEx;

//ÉùÒô²¥·ÅÆ÷
public class RecordAudioPlay extends AudioPlay {
	private IAudioPlayEventHandle _handle;
	private Thread _endCheckThread = null;
	private boolean _isPlayFinish = false;

	public RecordAudioPlay(int speakMode, IAudioPlayEventHandle handle) {
		super(speakMode, false);
		_handle = handle;
	}

	public void Play(MediaFrame[] mfs) {
		_handle.PlayBegin();
		for (MediaFrame mf : mfs)
			Play(mf);
		_endCheckThread = ThreadEx.GetThreadHandle(new CallBack(this, "EndCheckThread"));
		_endCheckThread.start();
	}

	public void EndCheckThread() {
		while (qFrames.size() > 0 || qDecQueue.size() > 0)
			ThreadEx.sleep(10);
		synchronized (this) {
			if (!_isPlayFinish) {
				_isPlayFinish = true;
				_handle.PlayEnd();

			}
		}

	}

	public void Stop() {
		ThreadEx.stop(_endCheckThread);
		synchronized (this) {
			if (!_isPlayFinish) {
				_isPlayFinish = true;
				_handle.PlayEnd();
			}
		}
		super.Stop();
	}

	public static interface IAudioPlayEventHandle {
		void PlayBegin();

		void PlayEnd();
	}

}
