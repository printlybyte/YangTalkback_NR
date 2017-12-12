package yangTalkback.Net;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import AXLib.Utility.CallBack;
import AXLib.Utility.Console;
import AXLib.Utility.Event;
import AXLib.Utility.Event.EventReceiver;
import AXLib.Utility.EventArg;
import AXLib.Utility.IAction;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.StreamSocket;
import AXLib.Utility.ThreadEx;
import AXLib.Utility.TimeUtil;
import yangTalkback.App.AppConfig;
import yangTalkback.Comm._DebugEx;
import yangTalkback.Media.MediaFrame;
import yangTalkback.Protocol.PBCallC;
import yangTalkback.Protocol.PBCallClosureC;
import yangTalkback.Protocol.PBCallClosureR;
import yangTalkback.Protocol.PBCallR;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdM;
import yangTalkback.Protocol.PBCmdR;
import yangTalkback.Protocol.PBHeart;
import yangTalkback.Protocol.PBLogoutR;
import yangTalkback.Protocol.PBMedia;
import yangTalkback.Protocol.PBMediaPart;
import yangTalkback.Protocol.PBMonitorCloseC;
import yangTalkback.Protocol.PBMonitorCloseR;
import yangTalkback.Protocol.PBMonitorOpenC;
import yangTalkback.Protocol.PBMonitorOpenR;
import yangTalkback.Protocol.Packet;
import yangTalkback.Protocol.StreamParser;

//连接基类
public abstract class Connection {

	private Date _lastActiveTime;
	private boolean _isConnected = false;// 连接状态
	private Thread _checkTimeoutThrad = null;// 超时检测线程
	protected int _timeoutSpan = 300;
	public Date SockConnectTime = new java.util.Date(0);// 连接时间
	public StreamSocket Sock;// SOCKET对象
	public StreamParser SParser;// 封包解析

	private PBMedia _parkPBMedia = null;// 封包重组对象
	private ByteArrayOutputStream _parkFrameDataStream = null;// 封包重组流

	public final Event<Exception> Error = new Event<Exception>();// 异常引发
	public final Event<Object> Connected = new Event<Object>();// 成功连接引发
	public final Event<Object> Disconnected = new Event<Object>();// 断开引发
	public final Event<Packet> Received = new Event<Packet>();// 接收到包引发
	public final Event<Object> Timeout = new Event<Object>();// 超时引发
	public final Event<PBLogoutR> Logouted = new Event<PBLogoutR>();// 异地登录

	public Connection(StreamSocket sock) {
		ResetSocket(sock);
	}

	public void ResetSocket(StreamSocket sock) {
		_isConnected = true;
		_lastActiveTime = TimeUtil.GetCurrentUtilDate();
		this.SockConnectTime = new java.util.Date();
		this.Sock = sock;
		if (this.SParser != null) {
			this.SParser.Readed.remove(SParser_Readed);
			this.SParser.Error.remove(this, "SParser_Error");
			this.SParser.Stop();
			this.SParser = null;
			_parkPBMedia = null;
			_parkFrameDataStream = null;
		}

		ThreadEx.stop(_checkTimeoutThrad);

		this.SParser = new StreamParser(sock);
		this.SParser.Readed.add(SParser_Readed);
		this.SParser.Error.add(this, "SParser_Error");
		this.SParser.Start();

		_checkTimeoutThrad = ThreadEx.GetThreadHandle(new CallBack(this, "CheckTimeoutThread"), "网络通信状态检测线程");
		_checkTimeoutThrad.start();
	}

	public void Disconnect() {
		_isConnected = false;
		ThreadEx.stop(_checkTimeoutThrad);
		this.SParser.Readed.remove(SParser_Readed);
		this.SParser.Error.remove(this, "SParser_Error");
		try {
			Sock.close();
		} catch (Exception e) {
		}
		OnDisconnected();
	}

	public void Reconnect() {

	}

	protected void OnConnected() {
		this.Connected.Trigger(this, null);
	}

	protected void OnDisconnected() {
		this.Disconnected.Trigger(this, null);

	}

	protected void OnError(Exception e) {
		_DebugEx.Trace("Connection", String.format("OnError:%1$s", e.getMessage()));
		this.Error.Trigger(this, e);

	}

	protected abstract void OnCallC(PBCallC pb);

	protected abstract void OnCallR(PBCallR pb);

	protected abstract void OnCallClosureC(PBCallClosureC pb);

	protected abstract void OnCallClosureR(PBCallClosureR pb);

	protected abstract void OnMonitorOpenC(PBMonitorOpenC pb);

	protected abstract void OnMonitorOpenR(PBMonitorOpenR pb);

	protected abstract void OnMonitorCloseC(PBMonitorCloseC pb);

	protected abstract void OnMonitorCloseR(PBMonitorCloseR pb);

	protected abstract void OnCmdC(PBCmdC pb);

	protected abstract void OnCmdM(PBCmdM pb);

	protected abstract void OnCmdR(PBCmdR pb);

	protected abstract void OnHeart(PBHeart pb);

	protected void OnLogout(PBLogoutR pb) {
		this.Logouted.Trigger(this, pb);
	}

	protected void OnTimeout() {
		Timeout.Trigger(this, new Object());
		// Disconnect();
	}

	protected abstract void OnMedia(PBMedia pb);

	protected void OnPacketReaded(Packet pack) {
		_lastActiveTime = TimeUtil.GetCurrentUtilDate();
		this.Received.Trigger(this, pack);
		switch (pack.MsgType) {
		case Call_C:
			OnCallC((PBCallC) ((pack.Body instanceof PBCallC) ? pack.Body : null));
			break;
		case Call_R:
			OnCallR((PBCallR) ((pack.Body instanceof PBCallR) ? pack.Body : null));
			break;
		case CallClosureC:
			OnCallClosureC((PBCallClosureC) ((pack.Body instanceof PBCallClosureC) ? pack.Body : null));
			break;
		case CallClosureR:
			OnCallClosureR((PBCallClosureR) ((pack.Body instanceof PBCallClosureR) ? pack.Body : null));
			break;
		case Heart_C:
			OnHeart((PBHeart) ((pack.Body instanceof PBHeart) ? pack.Body : null));
			break;
		case Media:
			OnMedia((PBMedia) ((pack.Body instanceof PBMedia) ? pack.Body : null));
			break;
		case MonitorOpen_C:
			OnMonitorOpenC((PBMonitorOpenC) ((pack.Body instanceof PBMonitorOpenC) ? pack.Body : null));
			break;
		case MonitorOpen_R:
			OnMonitorOpenR((PBMonitorOpenR) ((pack.Body instanceof PBMonitorOpenR) ? pack.Body : null));
			break;
		case MonitorClose_C:
			OnMonitorCloseC((PBMonitorCloseC) ((pack.Body instanceof PBMonitorCloseC) ? pack.Body : null));
			break;
		case MonitorClose_R:
			OnMonitorCloseR((PBMonitorCloseR) ((pack.Body instanceof PBMonitorCloseR) ? pack.Body : null));
			break;
		case Logout_R:
			OnLogout((PBLogoutR) ((pack.Body instanceof PBLogoutR) ? pack.Body : null));
			break;
		case Cmd_C:
			OnCmdC((PBCmdC) ((pack.Body instanceof PBCmdC) ? pack.Body : null));
			break;
		case Cmd_R:
			OnCmdR((PBCmdR) ((pack.Body instanceof PBCmdR) ? pack.Body : null));
			break;
		case Cmd_M:
			OnCmdM((PBCmdM) ((pack.Body instanceof PBCmdM) ? pack.Body : null));
			break;
		}
	}

	private EventReceiver<Packet> SParser_Readed = new EventReceiver<Packet>(new IAction<EventArg<Packet>>() {
		@Override
		public void invoke(EventArg<Packet> obj) {
			OnPacketReaded(obj.e);
		}
	});

	protected PBMedia AnalyzeMediaPark(PBMedia pb) {
		if (pb.Part == PBMediaPart.First) {

			if (_parkFrameDataStream != null)
				_parkFrameDataStream.reset();
			_parkPBMedia = pb;
			_parkFrameDataStream = new ByteArrayOutputStream();
			_parkFrameDataStream.write(pb.PartData, 0, pb.PartData.length);
		} else if (pb.Part == PBMediaPart.Mid) {
			if (_parkFrameDataStream != null) {
				_parkFrameDataStream.write(pb.PartData, 0, pb.PartData.length);
			} else {
				if (AppConfig._D)
					throw RuntimeExceptionEx.Create("");

			}
		} else if (pb.Part == PBMediaPart.End) {
			if (_parkFrameDataStream != null) {
				_parkFrameDataStream.write(pb.PartData, 0, pb.PartData.length);
				try {
					byte[] bytes = _parkFrameDataStream.toByteArray();
					_parkPBMedia.Frame = new MediaFrame(bytes);
					if (_parkFrameDataStream != null)
						_parkFrameDataStream.close();

					_parkFrameDataStream = null;
					PBMedia result = _parkPBMedia;
					_parkPBMedia = null;

					if (result.Frame.nSize != result.Frame.Data.length) {
						if (AppConfig._D)
							throw RuntimeExceptionEx.Create("");
						return null;
					} else {
						result.Part = PBMediaPart.Complete;
						return result;
					}
				} catch (Exception e) {
					if (AppConfig._D)
						throw RuntimeExceptionEx.Create(e);
				}
			} else {
				if (AppConfig._D)
					throw RuntimeExceptionEx.Create("");
			}
		}
		return null;
	}

	public void SParser_Error(EventArg<Exception> arg) {
		OnError(arg.e);
		Disconnect();
	}

	public void Send(Packet pack) {
		SParser.SendPack(pack);
	}

	public void CheckTimeoutThread() {
		while (_isConnected) {
			Date timeOut = TimeUtil.AddSeconds(_lastActiveTime, _timeoutSpan);
			Date nowDate = TimeUtil.GetCurrentUtilDate();
			if (TimeUtil.XYTime(timeOut, nowDate)) {
				Console.d("timeOut", String.format("t1:%s  t2:%s", _lastActiveTime.toString(), nowDate.toString()));
				OnTimeout();
				break;
			}
			ThreadEx.sleep(1000);
		}
	}

}