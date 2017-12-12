package yangTalkback.Net;
//package AXVChat.Net;
//
//import java.io.IOException;
//import java.util.Date;
//
//import AXLib.Model.RefObject;
//import AXLib.Utility.AQueue;
//import AXLib.Utility.BitConverter;
//import AXLib.Utility.CallBack;
//import AXLib.Utility.Event;
//import AXLib.Utility.RuntimeExceptionEx;
//import AXLib.Utility.StreamSocket;
//import AXLib.Utility.ThreadEx;
//import AXLib.Utility.TimeUtil;
//import AXVChat.Global;
//import AXVChat.Act.R.string;
//import AXVChat.App.AppConfig;
//import AXVChat.Media.MediaFrame;
//import AXVChat.Protocol.PBMedia;
//import AXVChat.Protocol.Packet;
//
//public class RemoteConnection {
//	public boolean IsConnected = false;
//	private StreamSocket _sock = null;
//	private boolean _working = false;
//	private boolean _publishFinish = false;
//	private Thread _sendThread = null;
//	private Thread _receThread = null;
//	private Date _lastActiveTime = null;
//	private AQueue<MediaFrame> _qAudio = new AQueue<MediaFrame>();
//	private AQueue<MediaFrame> _qVideo = new AQueue<MediaFrame>();
//	public final Event<Exception> Error = new Event<Exception>();
//	public final Event<Object> Disconnected = new Event<Object>();
//
//	public void Start() {
//		if (!IsConnected)
//			throw new RuntimeExceptionEx("未连接");
//		if (_working)
//			return;
//		_working = true;
//		_sendThread = ThreadEx.GetThreadHandle(new CallBack(this, "SendThread"));
//		_sendThread.start();
//		_receThread = ThreadEx.GetThreadHandle(new CallBack(this, "ReceThread"));
//		_receThread.start();
//	}
//
//	public boolean Prepare(String id, RefObject<String> refMsg) {
//		if (IsConnected) {
//			int flag;
//			try {
//				String cmd = "publish";
//				byte[] buf = BitConverter.GetBytes(cmd);
//				_sock.writeInt(buf.length);
//				_sock.write(buf);
//				flag = _sock.readInt();
//				if (flag == 0) {
//					buf = BitConverter.GetBytes(id);
//					_sock.writeInt(buf.length);
//					_sock.write(buf);
//					return true;
//				} else if (flag == -1) {
//					refMsg.Value = "当前其他连接正在发布视频";
//					return false;
//				} else {
//					throw new RuntimeExceptionEx("状态异常");
//				}
//			} catch (Exception e) {
//				throw new RuntimeExceptionEx("e");
//			}
//		}
//		return false;
//	}
//
//	public void Stop() {
//		if (!_working)
//			return;
//		_working = false;
//		ThreadEx.stop(_sendThread);
//		ThreadEx.stop(_receThread);
//		_qAudio.clear();
//		_qVideo.clear();
//		Disconnect();
//
//	}
//
//	public void Connect(String ip, int port) {
//		_sock = new StreamSocket();
//		_sock.connect(ip, port, 15 * 1000);
//		IsConnected = true;
//
//	}
//
//	public void Disconnect() {
//		IsConnected = false;
//		OnDisconnected();
//	}
//
//	public void PushMedia(MediaFrame frame) {
//		if (frame.nIsAudio == 1)
//			_qAudio.Enqueue(frame);
//		else
//			_qVideo.Enqueue(frame);
//	}
//
//	public void Finish() {
//		try {
//			synchronized (this) {
//				_publishFinish = true;
//				_sock.writeInt(-1);
//			}
//		} catch (Exception e) {
//			OnError(e);
//		}
//	}
//
//	public void SendThread() {
//		while (_working) {
//			try {
//				if (_qAudio.size() > 0) {
//					MediaFrame frame = _qAudio.Dequeue();
//					SendMedia(frame);
//				} else if (_qVideo.size() > 0) {
//					MediaFrame frame = null;
//					// 当视频缓冲队列桢数达到最大值及已发送第一个视频包则丢弃到最后一个关键桢
//					if (_qVideo.size() > Global.Instance.VideoSendQueueMax) {
//						frame = _qVideo.Dequeue();
//						if (frame.IsAllowDiscard()) {
//							MediaFrame[] ps = new MediaFrame[_qVideo.size()];
//							_qVideo.toArray(ps);
//							for (int i = ps.length - 1; i >= 0; i--) {
//								if (ps[i].nIsKeyFrame == 1) // 判断是否为关键桢
//								{
//									frame = ps[i];
//									// 清除队列到最后一个关键桢
//									while (_qVideo.size() > 0 && frame != _qVideo.Dequeue())
//										;
//									break;
//								}
//							}
//						}
//					} else {
//						frame = _qVideo.Dequeue();
//					}
//					SendMedia(frame);
//				} else {
//					ThreadEx.sleep(10);
//				}
//			} catch (Exception e) {
//				OnError(e);
//			}
//		}
//	}
//
//	public void ReceThread() {
//
//		try {
//			while (_working) {
//				int len = _sock.readInt();
//				_lastActiveTime = TimeUtil.getCurrentUtilDate();
//			}
//		} catch (Exception e) {
//			OnError(e);
//		}
//
//	}
//
//	private void SendMedia(MediaFrame frame) {
//		try {
//			byte[] buf = frame.getBytes();
//			synchronized (this) {
//				if (!_publishFinish) {
//					_sock.writeInt(buf.length);
//					_sock.write(buf);
//				}
//			}
//		} catch (Exception e) {
//			OnError(e);
//		}
//	}
//
//	protected void OnError(Exception e) {
//		Error.Trigger(this, e);
//		Disconnect();
//	}
//
//	protected void OnDisconnected() {
//		Disconnected.Trigger(this, null);
//	}
//
//}
