package yangTalkback.Net;

import yangTalkback.Global;
import yangTalkback.App.App;
import yangTalkback.Comm.*;
import yangTalkback.Protocol.*;

import AXLib.Model.*;
import AXLib.Utility.*;
import AXLib.Utility.Event.EventReceiver;

//当前客户端连接
public class ClientConnection extends Connection {

	private boolean _isLogined = false;// 是否已登录
	private boolean _connected = false;// 是否已连接
	private WaitResult<PBLoginR> _loginSemaphore = new WaitResult<PBLoginR>();// 登录同步锁
	private PBLoginR _pbLoginR = null;// 登录返回结果
	private Thread _heartThread;// 心跳线程

	private ListEx<IDModel> _allIDCache = null;
	private Object _cmdLock = new Object();
	private String _pwd;
	public short ID;// 当前ID
	public String Name;// 当前姓名

	public final boolean getIsConnected() {
		return _connected;
	}

	public final boolean getIsLogined() {
		return _isLogined;
	}

	public ClientConnection(StreamSocket sock) {

		super(sock);

		ID = (short) 0xffff;
		_connected = true;

	}

	public final boolean Login(short ID, String pwd, String name, boolean relogin, RefObject<String> msg) {
		return Login(ID, pwd, name, relogin, this._timeoutSpan, msg);
	}

	public final boolean Login(short ID, String pwd, String name, boolean relogin, int timeoutSpan, RefObject<String> msg) {
		_pbLoginR = null;
		PBLoginC tempVar = new PBLoginC();
		tempVar.ID = ID;
		tempVar.To = 0;
		tempVar.Pwd = pwd;
		tempVar.Name = name;
		tempVar.From = ID;
		tempVar.Relogin = relogin;
		tempVar.TimeoutSpan = timeoutSpan;
		this._timeoutSpan = timeoutSpan;
		Send(new Packet(MessageType.Login_C, tempVar));
		_loginSemaphore.Wait(1000 * 15);
		if (_pbLoginR != null) {
			msg.Value = _pbLoginR.Message;

			Boolean result = _pbLoginR.Result;
			if (result) {
				this.ID = ID;
				this.Name = name;
				this._pwd = pwd;
				this._isLogined = true;
				_heartThread = ThreadEx.GetThreadHandle(new CallBack(this, "HeartThread"));
				_heartThread.start();

			}
			return result;
		} else {
			msg.Value = "等待服务器响应超时";
			_DebugEx.Trace("ClientConnection", "等待LoginR包超时");
			return false;
		}
	}

	// 获取所在号码
	public final ListEx<IDModel> GetAllID() {
		final WaitResult<PBAllIDR> sp = new WaitResult<PBAllIDR>();
		try {
			EventReceiver<Packet> fun = new EventReceiver<Packet>(new IAction<EventArg<Packet>>() {
				public void invoke(EventArg<Packet> e) {
					Packet pack = e.e;
					if (pack.MsgType == MessageType.AllID_R) {
						sp.Finish((PBAllIDR) pack.Body);
					}
				}
			});
			this.Received.add(fun);
			PBAllIDC tempVar = new PBAllIDC();
			tempVar.From = ID;
			tempVar.To = 0;
			Send(new Packet(MessageType.AllID_C, tempVar));
			sp.Wait(Global.Instance.ConnectionCommandReturnTimeOut);
			this.Received.remove(fun);
			PBAllIDR pb = sp.Result;
			if (pb != null)
				_allIDCache = pb.IDList.ToList();
			return pb != null ? pb.IDList : null;
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			return new ListEx<IDModel>();
		}

	}

	// 获取所在号码
	public final ListEx<IDModel> GetAllIDByCache() {
		if (_allIDCache != null)
			return _allIDCache.ToList();
		else
			return new ListEx<IDModel>();
	}

	// 呼叫某一号码
	public final void Call(short toID) {
		PBCallC tempVar = new PBCallC();
		tempVar.From = ID;
		tempVar.To = toID;
		Send(new Packet(MessageType.Call_C, tempVar));
	}

	// 呼叫关闭
	public final void CallClosure(short toID, int cause, String msg) {
		PBCallClosureC tempVar = new PBCallClosureC();
		tempVar.From = ID;
		tempVar.To = toID;
		tempVar.Cause = cause;
		tempVar.Message = msg;
		Send(new Packet(MessageType.CallClosureC, tempVar));
	}

	public final void MonitorClose(short toID, int cause, String msg) {
		PBMonitorCloseC tempVar = new PBMonitorCloseC();
		tempVar.From = ID;
		tempVar.To = toID;
		tempVar.Cause = cause;
		tempVar.Message = msg;
		Send(new Packet(MessageType.MonitorClose_C, tempVar));
	}

	public final void Heart() {

		PBHeart tempVar = new PBHeart();
		tempVar.From = ID;
		tempVar.To = 0;
		tempVar.TalkStatus = App.TalkbackStatus.getValue();
		tempVar.MonitorPublishStatus = 0;
		//tempVar.TalkbackStatus = App.TalkbackStatus.getValue();
		Send(new Packet(MessageType.Heart_C, tempVar));
	}

	public final void PushMedia(PBMedia pb) {
		if (!_connected && !_isLogined)
			return;

		Send(new Packet(MessageType.Media, pb));
	}

	public final void PushMediaTCPMode(PBMedia pb) {
		if (!_connected && !_isLogined)
			return;

		Send(new Packet(MessageType.Media, pb));
	}

	public PBCmdR CmdC(PBCmdC pb) {
		synchronized (_cmdLock) {
			final WaitResult<PBCmdR> sp = new WaitResult<PBCmdR>();
			try {
				EventReceiver<Packet> fun = new EventReceiver<Packet>(new IAction<EventArg<Packet>>() {
					public void invoke(EventArg<Packet> e) {
						Packet pack = e.e;
						if (pack.MsgType == MessageType.Cmd_R) {
							sp.Finish((PBCmdR) pack.Body);
						}
					}
				});
				this.Received.add(fun);
				Send(new Packet(MessageType.Cmd_C, pb));
				sp.Wait(Global.Instance.ConnectionCommandReturnTimeOut);
				this.Received.remove(fun);
				PBCmdR pbresult = sp.Result;
				return pbresult;
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				return null;
			}
		}
	}

	public void CmdR(PBCmdR pb) {
		Send(new Packet(MessageType.Cmd_R, pb));
	}

	public void CmdM(PBCmdM pb) {
		Send(new Packet(MessageType.Cmd_M, pb));
	}

	@Override
	protected void OnDisconnected() {
		_isLogined = false;
		_connected = false;
		ThreadEx.waitStop(_heartThread, 200);
		super.OnDisconnected();
	}

	@Override
	protected void OnCallC(PBCallC pb) {
		if (App.LastAct != null) {
			RefObject<String> refObj = new RefObject<String>(null);
			boolean r = App.LastAct.CallC(pb, refObj);
			PBCallR tempVar = new PBCallR();
			tempVar.From = ID;
			tempVar.To = pb.From;
			tempVar.Result = r;
			tempVar.Message = refObj.Value;
			Send(new Packet(MessageType.Call_R, tempVar));
		}
	}

	@Override
	protected void OnCallR(PBCallR pb) {
		if (App.LastAct != null)
			App.LastAct.CallR(pb);
	}

	@Override
	protected void OnCallClosureC(PBCallClosureC pb) {
		if (App.LastAct != null)
			App.LastAct.CallClosureC(pb);
	}

	@Override
	protected void OnCallClosureR(PBCallClosureR pb) {
	}

	protected void OnMonitorOpenC(PBMonitorOpenC pb) {
		RefObject<String> refObj = new RefObject<String>(null);
		boolean result = App.LastAct.MonitorOpenC(pb, refObj);
		PBMonitorOpenR tempVar = new PBMonitorOpenR();
		tempVar.From = ID;
		tempVar.To = pb.From;
		tempVar.Result = result;
		tempVar.Message = refObj.Value;
		Send(new Packet(MessageType.MonitorOpen_R, tempVar));

	}

	protected void OnMonitorOpenR(PBMonitorOpenR pb) {
		throw RuntimeExceptionEx.Create("not imp");
	}

	protected void OnMonitorCloseC(PBMonitorCloseC pb) {
		App.LastAct.MonitorCloseC(pb);
		PBMonitorOpenR tempVar = new PBMonitorOpenR();
		tempVar.From = ID;
		tempVar.To = pb.From;
		tempVar.Result = true;
		tempVar.Message = "";
		Send(new Packet(MessageType.MonitorClose_R, tempVar));
	}

	protected void OnMonitorCloseR(PBMonitorCloseR pb) {
		throw RuntimeExceptionEx.Create("not imp");
	}

	protected void OnCmdC(PBCmdC pb) {
		throw RuntimeExceptionEx.Create("not imp");
	}

	protected void OnCmdM(PBCmdM pb) {
		App.LastAct.OnReceiveCmdM(pb);
	}

	protected void OnCmdR(PBCmdR pb) {

	}

	@Override
	protected void OnHeart(PBHeart pb) {
	}

	@Override
	protected void OnMedia(PBMedia pb) {
		if (pb.Part != PBMediaPart.Complete)
			pb = AnalyzeMediaPark(pb);

		if (pb != null) {
			if (App.LastAct != null)
				App.LastAct.MediaPushIn(pb);
		}
	}

	@Override
	protected void OnPacketReaded(Packet pack) {
		if (!_isLogined) {
			if (pack.MsgType == MessageType.Login_R) {
				_pbLoginR = ((PBLoginR) pack.Body);
				_loginSemaphore.Finish(_pbLoginR);
			}
		}
		super.OnPacketReaded(pack);
	}

	public final void HeartThread() {
		int count = 0;
		while (_isLogined && _connected) {
			ThreadEx.sleep(100);
			if (count-- <= 0) {
				Heart();
				count = Global.Instance.ClientHeartTimespace * 10;
			}
		}
	}
}