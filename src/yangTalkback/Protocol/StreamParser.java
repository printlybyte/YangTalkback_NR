package yangTalkback.Protocol;

import java.io.*;

import yangTalkback.*;
import yangTalkback.App.AppConfig;
import yangTalkback.Comm.*;

import AXLib.Utility.*;

public class StreamParser {
	final int MAX_MEDIAFRAME_PARK = 320 * 4;// 最大分包
	static java.util.HashMap<MessageType, java.lang.Class> _dicType = new java.util.HashMap<MessageType, java.lang.Class>();

	private StreamSocket _stream = null;// 网络数据收发器
	private StreamSocket _reader = null;// 网络数据收发器
	private StreamSocket _writer = null;// 网络数据收发器
	private boolean isAllowDiscard = false;//是否允许丢包
	private boolean _isPartMode = true;// 分包模式，该模式只在音频优先模式下起作用
	private int _sendMode = 0;// 0音频优先，1顺序发送
	private boolean _isWorking = false;
	private Thread _analyzeThread = null;
	private Thread _readThread = null;
	private Thread _sendThread = null;
	private Packet _lastVideoPacketPartObj = null;
	private AQueue<PBMedia> _qVideoPacketPark = new AQueue<PBMedia>();// 媒体包分段队列
	private AQueue<Packet> _qMsg = new AQueue<Packet>();// 通信消息队列
	private AQueue<Packet> _qVideo = new AQueue<Packet>();// 发送的视频队列,只在音频优先模式下使用
	private AQueue<Packet> _qAudio = new AQueue<Packet>();// 发送的音频队列,只在音频优先模式下使用
	private AQueue<Packet> _qMedia = new AQueue<Packet>();// 发送的音频队列,只在顺序发送模式下使用
	private AQueue<byte[]> _qReceive = new AQueue<byte[]>();// 接收队列
	private WaitResult<Object> _sendSemaphore = new WaitResult<Object>();
	private Object _syncReceive = new Object();
	private Object _syncSend = new Object();
	public final Event<Packet> Readed = new Event<Packet>();// 读取一个完整的包引发该事件
	public final Event<Exception> Error = new Event<Exception>();// 发生异常时引发该事件

	static {
		// 根据类型解成相应的PBBody对象，一般的PBBody为JSON序列化的字符串

		_dicType.put(MessageType.Login_C, PBLoginC.class);
		_dicType.put(MessageType.Login_R, PBLoginR.class);
		_dicType.put(MessageType.Logout_C, PBLogoutC.class);
		_dicType.put(MessageType.Logout_R, PBLogoutR.class);
		_dicType.put(MessageType.Heart_C, PBHeart.class);
		_dicType.put(MessageType.Heart_R, PBHeart.class);
		_dicType.put(MessageType.Call_C, PBCallC.class);
		_dicType.put(MessageType.Call_R, PBCallR.class);
		_dicType.put(MessageType.AllID_C, PBAllIDC.class);
		_dicType.put(MessageType.AllID_R, PBAllIDR.class);
		_dicType.put(MessageType.CallClosureC, PBCallClosureC.class);
		_dicType.put(MessageType.CallClosureR, PBCallClosureR.class);
		_dicType.put(MessageType.Media, PBCallR.class);
		_dicType.put(MessageType.MonitorOpen_C, PBMonitorOpenC.class);
		_dicType.put(MessageType.MonitorOpen_R, PBMonitorOpenR.class);
		_dicType.put(MessageType.MonitorClose_C, PBMonitorCloseC.class);
		_dicType.put(MessageType.MonitorClose_R, PBMonitorCloseR.class);
		_dicType.put(MessageType.Cmd_C, PBCmdC.class);
		_dicType.put(MessageType.Cmd_M, PBCmdM.class);
		_dicType.put(MessageType.Cmd_R, PBCmdR.class);
	}

	public StreamParser(StreamSocket stream) {
		_stream = stream;
		_reader = _stream;
		_writer = _stream;
	}

	public final void Start() {
		synchronized (this) {
			if (_isWorking) {
				return;
			}
			_isWorking = true;

			_readThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "ReadThread"), "网络数据接收线程"));
			_sendThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "SendThread"), "网络数据发送线程"));
			_analyzeThread = new Thread(ThreadEx.GetThreadHandle(new CallBack(this, "AnalyzeThread"), "网络数据解析线程"));
			_readThread.start();
			_sendThread.start();
			_analyzeThread.start();
		}
	}

	public final void Stop() {
		synchronized (this) {
			if (!_isWorking) {
				return;
			}
			_isWorking = false;
			ThreadEx.stop(_readThread);
			ThreadEx.stop(_sendThread);
			ThreadEx.stop(_analyzeThread);

			_readThread = null;
			_sendThread = null;
			_analyzeThread = null;
		}

	}

	// 网络数据接收线程
	public void ReadThread() {
		while (_isWorking) {
			try {
				int len = _reader.readInt();
				byte[] buf = _reader.readFully(len);
				_qReceive.Enqueue(buf);
				synchronized (_syncReceive) {
					_syncReceive.notify();
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				OnError(e);
				break;
			}
		}
	}

	// 数据解析线程
	public void AnalyzeThread() {
		while (_isWorking) {
			if (_qReceive.size() > 0) {
				byte[] buf = _qReceive.Dequeue();
				if (buf == null)
					continue;
				Packet pack = ReadPacket(buf);
				// if (pack.MsgType == MessageType.Media)
				// _DebugEx.Trace("StreamParser", string.Format("RECE:{0}",
				// pack.ToString()));
				if (pack.MsgType != MessageType.Media)
					_DebugEx.Trace("StreamParser", String.format("RECE:%1$s", pack.toString()));
				this.Readed.Trigger(this, pack);
			} else {
				try {
					synchronized (_syncReceive) {
						_syncReceive.wait();
					}
				} catch (Exception e) {
					if (_isWorking) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						CLLog.Error(e);
						OnError(e);
						break;
					}
				}
			}
		}
	}

	public void SendThread() {
		while (_isWorking) {
			Packet pack = GetNextSendPack();
			if (pack != null) // 如果包不为空则发送
			{
				byte[] buf = pack.GetBytes();
				try {
					// _DebugEx.Trace("StreamParser", string.Format("发送一个{0}包",
					// pack.MsgType == MessageType.Media ? "媒体" : "命令"));

					_writer.writeInt(buf.length);
					_writer.write(buf);
					_writer.flush();
				} catch (Exception e) {
					String stack = RuntimeExceptionEx.GetStackTraceString(e);
					OnError(e);
					break;
				}
			} else {
				try {
					synchronized (_syncSend) {
						_syncSend.wait();
					}
				} catch (Exception e) {
					if (_isWorking) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						CLLog.Error(e);
						OnError(e);
						break;
					}
				}
			}
		}
	}

	private Packet GetNextSendPack() {
		Packet pack = GetNextSendPack_Msg();
		if (pack != null)
			return pack;

		pack = GetNextSendPack_Media();
		return pack;
	}

	private Packet GetNextSendPack_Msg() {
		Packet pack = null;
		if (_qMsg.size() > 0)// 优先发送消息包
			pack = _qMsg.Dequeue();
		return pack;
	}

	private Packet GetNextSendPack_Media() {
		if (_sendMode == 0)// 音频优先
			return GetNextSendPack_Media_AudioPriority();
		else if (_sendMode == 1)
			return GetNextSendPack_Media_Sequence();
		else
			throw new RuntimeExceptionEx("not imp");
	}

	// 顺序发送
	private Packet GetNextSendPack_Media_Sequence() {
		if (_qMedia.size() > 0)
			return _qMedia.Dequeue();
		return null;

	}

	// 音频优先模式
	private Packet GetNextSendPack_Media_AudioPriority() {
		Packet pack = GetNextSendPack_Media_AudioPriority_Audio();
		if (pack != null)
			return pack;
		pack = GetNextSendPack_Media_AudioPriority_Video();
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Audio() {
		Packet pack = null;
		AQueue<Packet> queue = _qAudio;
		if (queue.size() > 0)// 第二优先发送音频包
		{
			pack = _qAudio.Dequeue();
			if (((PBMedia) pack.Body).Frame.IsAllowDiscard()) {
				// 当声音缓冲队列桢数达到最大值已发送第一个视频包则清空
				if (_qAudio.size() > Global.Instance.AudioSendQueueMax && isAllowDiscard) {
					_DebugEx.Trace("StreamParser", "音频缓冲队列满，丢弃音频包");
					pack = queue.Dequeue();
					while (queue.size() > 0) {
						pack = queue.Dequeue();
						if (((PBMedia) pack.Body).Frame.IsAllowDiscard())// 判断是否为命令帧
							break;
					}
				}
			}
		}
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Video() {
		Packet pack = null;
		// 是否使用分包模式
		if (_isPartMode) {
			// 获取分包
			pack = GetNextSendPack_Media_AudioPriority_Video_Part();
			if (pack != null)
				return pack;
		}
		AQueue<Packet> queue = _qVideo;

		if (queue.size() > 0) {

			// 当视频缓冲队列桢数达到最大值及已发送第一个视频包则丢弃到最后一个关键桢
			if (_qVideo.size() > Global.Instance.VideoSendQueueMax) {
				pack = queue.Dequeue();
				if (((PBMedia) pack.Body).Frame.IsAllowDiscard()) {
					Packet[] ps = new Packet[queue.size()];
					queue.toArray(ps);
					for (int i = ps.length - 1; i >= 0; i--) {
						if (ps[i] != null && ps[i].Body != null) {
							PBMedia pbmedia = ((PBMedia) ps[i].Body);
							if (pbmedia != null && pbmedia.Frame.nIsKeyFrame == 1) // 判断是否为关键桢
							{
								pack = ps[i];
								// 清除队列到最后一个关键桢
								while (queue.size() > 0 && pack != queue.Dequeue())
									;
								break;
							}
						} else {
							if (AppConfig._D)
								throw new RuntimeExceptionEx("frame is null");
						}
					}
				}
			} else {
				pack = queue.Dequeue();
			}
			if (_isPartMode) {
				// 分包处理
				_lastVideoPacketPartObj = pack;
				ListEx<PBMedia> list = GetPBMediaParks((PBMedia) pack.Body);
				_qVideoPacketPark = new AQueue<PBMedia>(list);

				// 再取分包进行发送
				pack = GetNextSendPack_Media_AudioPriority_Video_Part();
			}
		}
		return pack;
	}

	private Packet GetNextSendPack_Media_AudioPriority_Video_Part() {
		Packet pack = _lastVideoPacketPartObj;
		if (pack != null) {
			// 分包队列里面是否还有分包
			if (_qVideoPacketPark.size() > 0) {
				pack.Body = _qVideoPacketPark.Dequeue();
				if (pack.Body == null)
					throw new RuntimeExceptionEx("");
			}
			// 如果所有分包都已经发送完了则重置参数
			if (_qVideoPacketPark.size() == 0) {
				_lastVideoPacketPartObj = null;
				_qVideoPacketPark = null;
			}

		}
		return pack;

	}

	// 将一个媒体帧包拆分
	private ListEx<PBMedia> GetPBMediaParks(PBMedia pb) {
		byte[] frameBuf = pb.Frame.GetBytes();
		if (frameBuf.length <= MAX_MEDIAFRAME_PARK) {
			ListEx<PBMedia> listEx = new ListEx<PBMedia>();
			listEx.add(pb);
			return listEx;
		}
		ByteArrayInputStream ms = new ByteArrayInputStream(frameBuf);
		LittleEndianDataInputStream br = new LittleEndianDataInputStream(ms);
		ListEx<PBMedia> list = new ListEx<PBMedia>();

		while (ms.available() > 0) {
			byte[] buf = null;
			try {
				if (ms.available() >= MAX_MEDIAFRAME_PARK)
					buf = br.readFully(MAX_MEDIAFRAME_PARK);
				else
					buf = br.readFully(ms.available());
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);
			}

			PBMedia item = new PBMedia();

			item.To = pb.To;
			item.From = pb.From;
			item.Message = pb.Message;
			item.Result = pb.Result;
			item.PartData = buf;

			if (list.size() == 0)
				item.Part = PBMediaPart.First;
			else if (ms.available() == 0)
				item.Part = PBMediaPart.End;
			else
				item.Part = PBMediaPart.Mid;
			list.add(item);
		}
		return list;

	}

	// 解析包
	protected Packet ReadPacket(byte[] packBuffer) {
		Packet pack = null;
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(packBuffer);
			LittleEndianDataInputStream _reader = new LittleEndianDataInputStream(stream);
			pack = new Packet();
			if (pack.HeadFlag != _reader.readByte())
				throw RuntimeExceptionEx.Create("HeadFlag出错");
			pack.MsgType = MessageType.forValue(_reader.readByte());
			pack.From = _reader.readShort();
			pack.To = _reader.readShort();
			int bodyBufLen = _reader.readInt();

			byte[] buf = _reader.readFully(bodyBufLen);
			if (pack.EndFlag != _reader.readByte())
				throw RuntimeExceptionEx.Create("EndFlag出错");
			pack.Body = ReadPacketBody(pack.MsgType, buf);

		} catch (IOException e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		return pack;
	}

	// 解析包
	protected PBodyBase ReadPacketBody(MessageType msgType, byte[] buf) {

		if (msgType != MessageType.Media) {
			String txt = BitConverter.ToString(buf, 0, buf.length);
			Object obj = JSONHelper.forJSON(txt, _dicType.get(msgType));
			return (PBodyBase) obj;
		} else {
			return new PBMedia(buf);
		}
	}

	// 发送包,放到相应的队列等待发送
	public final void SendPack(Packet pack) {

		if (pack.MsgType == MessageType.Media) {
			// _DebugEx.Trace("StreamParser", string.Format("SEND:{0}",
			// pack.ToString()));
			if (_sendMode == 0) {// 音频优先
				PBMedia body = (PBMedia) ((pack.Body instanceof PBMedia) ? pack.Body : null);
				if (body != null && body.Frame != null) {
					// 根据桢类型放到相应的对列
					if (body.Frame.nIsAudio == 1) {
						_qAudio.Enqueue(pack);
					} else {
						_qVideo.Enqueue(pack);
					}
				}
			} else if (_sendMode == 1) {// 顺序发送
				_qMedia.Enqueue(pack);
			} else {
				throw RuntimeExceptionEx.Create("");
			}
		} else {
			_DebugEx.Trace("StreamParser", String.format("SEND:%1$s", pack.toString()));
			_qMsg.Enqueue(pack);

		}
		synchronized (_syncSend) {
			_syncSend.notify();
		}
	}

	private void OnError(Exception e) {
		Error.Trigger(this, e);

	}
}