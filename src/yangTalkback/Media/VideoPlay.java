package yangTalkback.Media;

import yangTalkback.Codec.FFDecoder;
import yangTalkback.Codec.H264AndroidDecoder;
import yangTalkback.Codec.JPEGDecoder;
import yangTalkback.Codec.Cfg.VideoEncodeCfg;
import yangTalkback.Codec.FFCodec.FFCode;
import yangTalkback.Comm.*;
import yangTalkback.Media.VideoImage.ScaleMode;
import android.graphics.Bitmap;

import AXLib.Utility.CallBack;
import AXLib.Utility.IDisposable;
import AXLib.Utility.Queue;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
//import AXVChat.Codec.H264Decoder;

//视频播放对象
public class VideoPlay implements IDisposable {
	private H264AndroidDecoder _h264Dec = null;// H264解码器,没有用到
	private FFDecoder _ffDec = null;// FFMPEG解码器
	private JPEGDecoder _jpgDec = null;// 图片解码器
	private VideoImage _img = null;// 视频显示对象
	private Bitmap _bmp = null;// 显示缓存图片
	private int _decSelect = 1;// 0使用H264AndroidDecoder，1使用FFDecoder
	private VideoEncodeCfg _cfg = null;// 视频编码参数
	private boolean _inited = false;// 初始化状态
	private boolean _working = false;// 工作状态
	private boolean _isPlay = true;// 是否播放视频
	private Object _asynObj = new Object();
	private Queue<MediaFrame> _qPlay = new Queue<MediaFrame>();// 视频播放列队
	public boolean KeyFrameMode = false;// 是否只播放当键帧
	public Thread _playThread = null;// 播放线程

	public VideoPlay(VideoImage img) {
		this._img = img;
	}

	private void Init(MediaFrame mf) {
		if (mf.nIsKeyFrame != 1)
			return;
		_cfg = VideoEncodeCfg.Create(mf);
		if (_decSelect == 0) {
			_h264Dec = new H264AndroidDecoder();
		} else {
			// 根据编码器名称选择解码器
			if (_cfg.encodeName.equalsIgnoreCase("h264"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_H264);
			else if (_cfg.encodeName.equalsIgnoreCase("h263"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_H263);
			else if (_cfg.encodeName.equalsIgnoreCase("flv1"))
				_ffDec = new FFDecoder(FFCode.CODEC_ID_FLV1);
			else if (_cfg.encodeName.equalsIgnoreCase("JPEG"))
				_jpgDec = new JPEGDecoder();

			else {
				CLLog.Error("媒体参数编码类型错误");
				throw RuntimeExceptionEx.Create("媒体参数编码类型错误");
			}
		}
		_inited = true;
	}

	// 解码视频
	private VideoDisplayFrame Decode(MediaFrame mf) {
		VideoDisplayFrame vdFrame = null;
		if (KeyFrameMode && mf.nIsKeyFrame == 0)
			return null;
		try {
			if (!_inited)
				Init(mf);
			if (!_inited)
				return null;
			if (_jpgDec != null) {
				vdFrame = _jpgDec.Deocde(mf);
			} else {
				if (_decSelect == 0 && _h264Dec != null) {
					vdFrame = _h264Dec.Deocde(mf);
				} else if (_decSelect == 1 && _ffDec != null) {
					vdFrame = _ffDec.Deocde(mf);
				}
			}
		} catch (Exception e) {
			CLLog.Error("解码错误", e);
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create("解码错误", e);
		}
		return vdFrame;
	}

	// 播放
	public void Play(MediaFrame mf) {
		_qPlay.offer(mf);
		synchronized (_asynObj) {
			_asynObj.notify();
		}
	}

	// 播放线程
	public void PlayThread() {
		while (_working) {
			if (_qPlay.size() > 0) {
				MediaFrame mf = null;
				if (_qPlay.size() > 60) {
					while (_qPlay.size() != 20)
						_qPlay.poll();

					while (_qPlay.size() > 0) {
						mf = _qPlay.poll();
						if (mf != null && mf.nIsKeyFrame == 1) {
							break;
						}
					}
				} else {
					mf = _qPlay.poll();
				}

				if (mf == null)
					continue;
				VideoDisplayFrame vdFrame = Decode(mf);
				if (_isPlay && vdFrame != null)
					_img.Play(vdFrame);
			} else {
				try {
					synchronized (_asynObj) {
						_asynObj.wait();
					}
				} catch (Exception e) {
					if (_working) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						throw RuntimeExceptionEx.Create(e);
					}
				}
			}
		}
	}

	public void Start() {
		if (_working)
			return;
		_working = true;
		_playThread = ThreadEx.GetThreadHandle(new CallBack(this, "PlayThread"), "视频播放线程");
		_playThread.start();
	}

	public void Stop() {
		if (!_working)
			return;
		_working = false;
		ThreadEx.stop(_playThread);
		_qPlay.clear();
		Clean();
	}

	@Override
	public void Dispose() {
		try {
			Stop();
			if (_h264Dec != null)
				_h264Dec.Dispose();
			if (_ffDec != null)
				_ffDec.Dispose();
			if (_jpgDec != null)
				_jpgDec.Dispose();
		} catch (Throwable e) {
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}
	}

	public void Clean() {
		if (_img != null)
			_img.Clean();
	}

	public void PlaySwitch(boolean status) {
		_isPlay = status;
	}

	public void SetScaleMode(ScaleMode mode) {
		_img.SetScaleMode(mode);

	}

	public ScaleMode GetScaleMode() {
		return _img.Scale;

	}
}
