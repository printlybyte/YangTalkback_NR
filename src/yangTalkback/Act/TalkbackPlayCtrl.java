package yangTalkback.Act;

import java.util.HashMap;

import AXLib.Utility.*;

import yangTalkback.Media.AudioPlay;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Media.MediaFrame.MediaFrameCommandType;
import yangTalkback.Net.Model.TalkbackChannelInfo;
import yangTalkback.Protocol.*;

public class TalkbackPlayCtrl {
	private int _speekMode = 0;
	private actTalkback _act;
	private short _fromId = -1;
	private boolean _isworking = false;
	private boolean _isTwoway = false;
	private boolean _isBtnDown = false;
	private boolean _twowayCanPlay = false;
	private boolean _sinwayCanPlay = false;
	private boolean _sinwayCanPlayStatus = true;
	private HashMap<Short, AudioPlay> _apMap = new HashMap<Short, AudioPlay>();
	private ListEx<Short> _twowayIDList = new ListEx<Short>();
	private AQueue<PBMedia> _qPBCache = new AQueue<PBMedia>();
	private TalkbackChannelInfo _chInfo = null;
	private Thread _playThread = null;
	private long _lastReceivePBMediaTime = -1;

	public TalkbackPlayCtrl(actTalkback act, short fromId, boolean isTwoway, int speekMode) {
		_act = act;
		_fromId = fromId;
		_speekMode = speekMode;
		_isTwoway = isTwoway;
		_twowayCanPlay = true;
	}

	public void SetTalkbackChannelInfo(TalkbackChannelInfo info) {
		_chInfo = info;
		_twowayIDList = info.TwowayIDList.ToList();
	}

	public void PushIn(PBMedia pb) {
		_lastReceivePBMediaTime = System.currentTimeMillis();
		_qPBCache.Enqueue(pb);
	}

	public void SetSinwayCanPlay(boolean status) {
		_sinwayCanPlayStatus = status;
	}

	public boolean PushIn_Twoway(PBMedia pb) {
		MediaFrame mf = pb.Frame;
		AudioPlay ap = _apMap.get(pb.From);
		ap.Play(mf);
		return true;
	}

	public boolean PushIn_Sinway(PBMedia pb) {
		MediaFrame mf = pb.Frame;
		AudioPlay ap = _apMap.get(pb.From);
		if (_twowayIDList.contains((Object) pb.From)) {// 如果讲话人使用双向
			// 如果当前讲话按钮按下，等讲完话后再播对方声音
			while (!_twowayCanPlay)
				ThreadEx.sleep(10);
			if (!mf.IsCommandFrame())// 过滤命令包
				ap.Play(mf);
		} else {
			if (_sinwayCanPlayStatus) {
				if (mf.IsCommandFrame()) {
					if (mf.GetCommandType() == MediaFrameCommandType.Start) {
						_sinwayCanPlay = true;
						_act.SetTalkButtonEnabled(false);
					} else if (mf.GetCommandType() == MediaFrameCommandType.Stop) {
						_sinwayCanPlay = false;
						_act.SetTalkButtonEnabled(true);
					}
				} else {
					if (_sinwayCanPlay)
						ap.Play(mf);
					else {
						if (_act.GetTalkButtonEnabled())//如果当前为可播放状态则返回TRUE
							return true;
						else
							return false;
					}
				}
			} else {
				return false;
			}

		}
		return true;
	}

	public void PlayThread() {
		while (_isworking) {
			if (_qPBCache.size() > 0) {
				PBMedia pb = _qPBCache.peek();
				if (pb != null) {
					boolean r = false;
					if (_isTwoway)
						r = PushIn_Twoway(pb);
					else {
						r = PushIn_Sinway(pb);

					}
					if (r)
						_qPBCache.Dequeue();
					else
						ThreadEx.sleep(10);
				}
			} else
				ThreadEx.sleep(10);
		}
	}

	public void SetTalkButtonDownStatus(boolean isDown) {
		_isBtnDown = isDown;
		_twowayCanPlay = !isDown;// 按下的时候不允许播对方双向
	}

	public void Start() {
		if (_isworking)
			return;
		_isworking = true;
		for (short id : _chInfo.OriginalIDList) {
			AudioPlay ap = new AudioPlay(_speekMode, true);
			_apMap.put(id, ap);
			ap.Start();
		}
		_playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"));
		_playThread.start();
	}

	public void Stop() {
		if (!_isworking)
			return;
		_isworking = false;

		ThreadEx.stop(_playThread);
		_playThread = null;
		_qPBCache.clear();
		for (AudioPlay ap : _apMap.values()) {
			ap.Stop();
			ap.Dispose();
		}
	}
}
