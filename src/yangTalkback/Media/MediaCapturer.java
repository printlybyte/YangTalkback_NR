package yangTalkback.Media;

import yangTalkback.App.AppConfig;
import yangTalkback.Codec.CameraEncoder;
import yangTalkback.Codec.CameraEncoderJPEG;
import yangTalkback.Codec.MicEncoder;
import yangTalkback.Codec.Cfg.AudioEncodeCfg;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;

import android.view.Surface;

import AXLib.Utility.Console;
import AXLib.Utility.Event;
import AXLib.Utility.EventArg;
import AXLib.Utility.IAction;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.StringEx;

//媒体采集器
public class MediaCapturer implements CameraEncoder.CameraEncoderDataReceiver, MicEncoder.MicEncoderDataReceiver {

	private CameraEncoder _ce;// 摄像头采集
	private MicEncoder _me;// 麦克风采集
	private boolean _isWorking = false;// 工作状态
	private boolean _firstAudioFristFrame = true;
	private boolean _firstVideoFristFrame = true;
	private VideoEncodeCfg _vcfg = null;// 视频参数
	private AudioEncodeCfg _acfg = null;// 音频参数
	IAction<MediaFrame> _captured = null;// 采集回调
	public boolean IsVideoPub = true;// 是否回调视频
	public boolean IsAudioPub = true;// 是否回调音频
	public final Event<Exception> Error = new Event<Exception>();// 当引常引发
	public IAction<MediaCapturer> OnStoped;// 当完全停止引发

	public MediaCapturer(VideoEncodeCfg vCfg, AudioEncodeCfg aCfg, IAction<MediaFrame> captured) {
		_vcfg = vCfg;
		_acfg = aCfg;
		_captured = captured;
		_ce = new CameraEncoder(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		if (StringEx.equalsIgnoreCase(vCfg.encodeName, "JPEG"))
			_ce = new CameraEncoderJPEG(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		else
			_ce = new CameraEncoder(vCfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		_me = new MicEncoder(aCfg, this);
		_ce.Error.add(this, "NS_Error");

	}

	public void Start() throws Exception {

		if (_isWorking)
			return;
		_isWorking = true;
		if (IsVideoPub)
			_ce.start();
		if (IsAudioPub)
			_me.start();
	}

	public void Stop() {
		if (!_isWorking)
			return;
		_isWorking = false;
		try {
			Console.d("STOP", "开始停止音频采集");
			_me.stop();
			Console.d("STOP", "完成停止音频采集");
			Console.d("STOP", "开始停止视频采集");
			_ce.stop();
			Console.d("STOP", "完成停止视频采集");
			if (OnStoped != null)
				OnStoped.invoke(this);
		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void AutoFocus() {
		if (_ce != null)
			_ce.AutoFocus();
	}

	@Override
	public void Received(MediaFrame frame) {

		if (_captured != null) {
			if (frame.nIsAudio == 1 && IsAudioPub) {
				if (_firstAudioFristFrame && !frame.IsCommandFrame()) {
					frame.nEx = 0;
					_firstAudioFristFrame = false;
				}
				_captured.invoke(frame);
			}
			if (frame.nIsAudio == 0 && IsVideoPub) {
				if (_firstVideoFristFrame && !frame.IsCommandFrame()) {
					frame.nEx = 0;
					_firstVideoFristFrame = false;
				}
				_captured.invoke(frame);
			}
		}

	}

	public void NS_Error(EventArg<Exception> arg) throws Exception {
		Stop();
		Error.Trigger(this, arg.e);
	}

	public void SetAudioSyncKey(String key) {
		if (_me != null)
			_me.SetAudioSyncKey(key);
		else {
			throw RuntimeExceptionEx.Create("MicEncoder未实例化");
		}
	}

	public void VideoSwitch(boolean status) {
		this.IsVideoPub = status;

	}

	public void AudioSwitch(boolean status) {
		this.IsAudioPub = status;

	}

	public boolean GetIsWorking() {
		return _isWorking;
	}

	public void PausePreview() {
		if (_ce != null) {
			_ce.Error.remove(this, "NS_Error");
			_ce.stop();
			_ce = null;
		}
	}

	public void RestartPreview(Surface surface) {
		if (_ce != null) {
			_ce.Error.remove(this, "NS_Error");
			_ce.stop();
			_ce = null;
		}
		_vcfg.surface = surface;
		_ce = new CameraEncoder(_vcfg, AppConfig.Instance.VideoCaptrueRestartMinutes, this);
		if (_isWorking)
			_ce.start();
	}

	public void ChangeCfg(VideoEncodeCfg vCfg, AudioEncodeCfg aCfg) {
		_vcfg = vCfg;
		_acfg = aCfg;
		RestartPreview(vCfg.surface);
	}
}
