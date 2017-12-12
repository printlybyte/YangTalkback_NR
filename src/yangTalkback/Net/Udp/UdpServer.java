package yangTalkback.Net.Udp;
//package AXVChat.Net.Udp;
//
//
//public class UdpServer {
//	private static final int _MAX = 1000;
//	private static final boolean _DEBUG = false;
//	private static java.util.HashMap<String, UdpPartCacheMode> _dicUdpPark = new java.util.HashMap<String, UdpPartCacheMode>();
//	private static Socket _sock = null;
//	private static Thread _receThread = null;
//	private static java.util.HashMap<Short, MediaPushInfo> _dicTimetick = new java.util.HashMap<Short, MediaPushInfo>();
//	private static AQueue<UdpPacket> _qCompelte = new AQueue<UdpPacket>();
//
//	public static ListEx<UdpPacket> UdpPacketDepart(UdpPacket pack) {
//		if (pack.PartType != UdpPacketPartType.Complete) {
//			throw new ApplicationException("pack error");
//		}
//		java.util.ArrayList<UdpPacket> list = new java.util.ArrayList<UdpPacket>();
//		java.util.ArrayList<Byte[]> bufs = new java.util.ArrayList<Byte[]>();
//		MemoryStream ms = new MemoryStream(pack.Data);
//		while (ms.Position < ms.getLength()) {
//			byte[] buf = null;
//			if (ms.getLength() - ms.Position >= _MAX) {
//				buf = new byte[_MAX];
//			} else {
//				buf = new byte[ms.getLength() - ms.Position];
//			}
//			ms.Read(buf, 0, buf.length);
//			bufs.add(buf);
//		}
//		for (int i = 1; i <= bufs.size(); i++) {
//			UdpPacket tempVar = new UdpPacket();
//			tempVar.ID = pack.ID;
//			tempVar.Index = (byte) (bufs.size() - i + 1);
//			tempVar.Data = bufs.get(i - 1);
//			UdpPacket tp = tempVar;
//			if (i == 1) {
//				tp.PartType = UdpPacketPartType.First;
//			} else if (i == bufs.size()) {
//				tp.PartType = UdpPacketPartType.Last;
//			} else {
//				tp.PartType = UdpPacketPartType.Mid;
//			}
//			list.add(tp);
//		}
//		if (list.size() == 1) {
//			list.get(0).PartType = UdpPacketPartType.Complete;
//		}
//		return list;
//
//	}
//
//	public static UdpPacket UdpPacketMarger(java.util.ArrayList<UdpPacket> list)
//	{
////C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
////C# TO JAVA CONVERTER TODO TASK: Lambda expressions and anonymous methods are not converted by C# to Java Converter:
//		var first = list.FirstOrDefault(p => p.PartType == UdpPacketPartType.First);
//		if (first == null)
//		{
//			if (_DEBUG)
//			{
//				throw new RuntimeException("first error");
//			}
//			return null;
//		}
//
////C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
////C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
////C# TO JAVA CONVERTER TODO TASK: Lambda expressions and anonymous methods are not converted by C# to Java Converter:
//		var idTest = list.Select(p => p.ID).Distinct().Count() == 1;
//		if (!idTest)
//		{
//			if (_DEBUG)
//			{
//				throw new RuntimeException("id error");
//			}
//			return null;
//		}
//
////C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
////C# TO JAVA CONVERTER TODO TASK: Lambda expressions and anonymous methods are not converted by C# to Java Converter:
//		list = list.OrderByDescending(p => p.Index).ToList();
//		for (int i = first.Index; i > 0; i--)
//		{
//			if (list.get(i).Index != i)
//			{
//				if (_DEBUG)
//				{
//					throw new RuntimeException("index error");
//				}
//				return null;
//			}
//		}
//
//		System.IO.MemoryStream ms = new System.IO.MemoryStream();
////C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
//		for (var item : list)
//		{
//			ms.Write(item.Data, 0, item.Data.getLength());
//		}
//
//		UdpPacket tempVar = new UdpPacket();
//		tempVar.Data = ms.toArray();
//		tempVar.ID = first.ID;
//		tempVar.Index = 1;
//		tempVar.PartType = UdpPacketPartType.Complete;
//		UdpPacket pack = tempVar;
//		return pack;
//
//	}
//
//	public static PBodyBase ConvPB(UdpPacket pack) {
//		if (pack.PacketType == UdpPacketType.Audio || pack.PacketType == UdpPacketType.Video) {
//			PBMedia pb = new PBMedia();
//			pb.SetBytes(pack.Data);
//			return pb;
//		} else {
//			return null;
//		}
//	}
//
//	public static boolean UdpPacketVerify(UdpPacket pack) {
//		return false;
//	}
//
//	public static void OnRece(String key, byte[] bytes) {
//		UdpPartCacheMode model = null;
//		synchronized (_dicUdpPark) {
//			if (_dicUdpPark.containsKey(key)) {
//				model = _dicUdpPark.get(key);
//			} else {
//				UdpPartCacheMode tempVar = new UdpPartCacheMode();
//				tempVar.setKey(key);
//				tempVar.setCreateTime(new java.util.Date());
//				_dicUdpPark.put(key, model = tempVar);
//			}
//		}
//
//		UdpPacket park = new UdpPacket();
//		park.SetBytes(bytes);
//		if (park.PacketType == UdpPacketType.Video) {
//			java.util.HashMap<Integer, UdpPacket> parks = null;
//			synchronized (model) {
//				if (model.Video.containsKey(park.ID)) {
//					parks = model.Video.get(park.ID);
//				} else {
//					model.Video.put(park.ID, parks = new java.util.HashMap<Integer, UdpPacket>());
//				}
//			}
//			synchronized (parks) {
//				parks.put(park.Index, park);
//				UdpPacket compeltePack = UdpPacketMarger(parks.values().ToList());
//				if (compeltePack != null && UdpPacketVerify(compeltePack)) {
//					synchronized (model) {
//						model.Video.remove(park.ID);
//					}
//					_qCompelte.Enqueue(compeltePack);
//				}
//			}
//		} else if (park.PacketType == UdpPacketType.Audio) {
//			_qCompelte.Enqueue(park);
//		}
//	}
//
//	public static void CompelteThread() {
//		while (true) {
//			if (_qCompelte.size() > 0) {
//				// C# TO JAVA CONVERTER TODO TASK: There is no equivalent to
//				// implicit typing in Java:
//				var pack = _qCompelte.Dequeue();
//				PBodyBase pb = ConvPB(pack);
//				OnPushMedia((PBMedia) pb);
//			}
//		}
//	}
//
//	private static void OnPushMedia(PBMedia pb) {
//		boolean needPush = false;
//		synchronized (_dicTimetick) {
//			if (_dicTimetick.containsKey(pb.From)) {
//				if (pb.Frame.nIsAudio == 0 && _dicTimetick.get(pb.From).VideoTimetick < pb.Frame.nTimetick) {
//					needPush = true;
//					_dicTimetick.get(pb.From).VideoTimetick = pb.Frame.nTimetick;
//				}
//				if (pb.Frame.nIsAudio == 1 && _dicTimetick.get(pb.From).AudioTimetick < pb.Frame.nTimetick) {
//					needPush = true;
//					_dicTimetick.get(pb.From).AudioTimetick = pb.Frame.nTimetick;
//				}
//			} else {
//				if (pb.Frame.nIsAudio == 0) {
//					needPush = true;
//					_dicTimetick.get(pb.From).VideoTimetick = pb.Frame.nTimetick;
//				}
//				if (pb.Frame.nIsAudio == 1) {
//					needPush = true;
//					_dicTimetick.get(pb.From).AudioTimetick = pb.Frame.nTimetick;
//				}
//			}
//		}
//		if (needPush) {
//			Service.PushMediaUDP(pb);
//		}
//	}
//
//	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//	// /#region UDP Data Receive
//
//	private static void Init() {
//		_sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
//		_sock.Bind(new IPEndPoint(IPAddress.Any, 6554));
//	}
//
//	public static void ReceThread() {
//		while (true) {
//			byte[] buf = new byte[2048];
//			EndPoint sender = new IPEndPoint(IPAddress.Any, 0);
//			RefObject<EndPoint> tempRef_sender = new RefObject<EndPoint>(sender);
//			int size = _sock.ReceiveFrom(buf, tempRef_sender);
//			sender = tempRef_sender.argvalue;
//			RefObject<Byte> tempRef_buf = new RefObject<Byte>(buf);
//			Array.<Byte> Resize(tempRef_buf, size);
//			buf = tempRef_buf.argvalue;
//			String key = String.format(((IPEndPoint) sender).Address.toString() + " " + ((IPEndPoint) sender).Port);
//			OnRece(key, buf);
//		}
//	}
//
//	// C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//	// /#endregion
//
//}