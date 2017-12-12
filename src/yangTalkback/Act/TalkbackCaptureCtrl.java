package yangTalkback.Act;

import yangTalkback.Codec.MicEncoder;
import yangTalkback.Codec.MicEncoder.MicEncoderDataReceiver;
import yangTalkback.Codec.Cfg.AudioEncodeCfg;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Protocol.*;

public class TalkbackCaptureCtrl implements MicEncoderDataReceiver {
	private int _speekMode = 0;
	private actTalkback _act;
	private short _fromId = -1;
	private boolean _isworking = false;
	private boolean _isTwoway = false;
	private boolean _isOpenCapture = false;
	private boolean _isNeedSendMediaCommandStart = false;// 是否需要发送开始语音
	private boolean _isNeedSendMediaCommandStop = false;// 是否需要发送停止语音
	private MicEncoder _me = null;
	private AudioEncodeCfg _cfg = null;

	public TalkbackCaptureCtrl(actTalkback act, short fromId, boolean isTwoway, int speekMode) {
		_act = act;
		_fromId = fromId;
		_speekMode = speekMode;
		_isTwoway = isTwoway;
		_isOpenCapture = isTwoway;
	}

	public void Start() {
		if (_isworking)
			return;
		_isworking = true;
		_cfg = AudioEncodeCfg.GetDefault();
		// _cfg.samples = 640;
		// _cfg.compression = 8;

		_me = new MicEncoder(_cfg, this);
		_me.start();
	}

	public void Stop() {
		if (!_isworking)
			return;
		_isworking = false;
		_me.stop();
	}

	public void Capture(boolean isOpen) {
		_isNeedSendMediaCommandStart = isOpen;
		_isNeedSendMediaCommandStop = !isOpen;
		_isOpenCapture = isOpen;
	}

	@Override
	public void Received(MediaFrame frame) {

		if (_isOpenCapture || _isNeedSendMediaCommandStart || _isNeedSendMediaCommandStop) {
			if (_isNeedSendMediaCommandStart) {
				PBMedia pb = new PBMedia(_fromId, (short) 0, MediaFrame.CreateCommandMediaFrame(true, MediaFrame.MediaFrameCommandType.Start));
				_act.PushOut(pb);
				_isNeedSendMediaCommandStart = false;
			}
			if (_isOpenCapture) {
				PBMedia pb = new PBMedia(_fromId, (short) 0, frame);
				_act.PushOut(pb);
			}
			if (_isNeedSendMediaCommandStop) {
				PBMedia pb = new PBMedia(_fromId, (short) 0, MediaFrame.CreateCommandMediaFrame(true, MediaFrame.MediaFrameCommandType.Stop));
				_act.PushOut(pb);
				_isNeedSendMediaCommandStop = false;
			}
		}

	}
}
