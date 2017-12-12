package yangTalkback.Protocol;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import yangTalkback.App.AppConfig;
import yangTalkback.Net.Udp.UdpPacket;
import yangTalkback.Net.Udp.UdpPacketPartType;
import yangTalkback.Net.Udp.UdpPacketType;

import AXLib.Utility.*;
import AXLib.Utility.Console;

public class UdpChannel {

	private DatagramSocket _sock = null;
	private String _ip = null;
	private int _port = 0;
	private InetSocketAddress _addr = null;
	private long _audioTick = 0;
	private long _videoTick = 0;

	public UdpChannel(String ip, int port) {
		_ip = ip;
		_port = port;
		_addr = new InetSocketAddress(ip, port);
		try {
			_sock = new DatagramSocket();
			_sock.setSendBufferSize(1024 * 1024 * 8);

		} catch (SocketException e) {

		}
	}

	public void SendPBMedia(PBMedia pb) {

		if (pb.Frame.nIsAudio == 0) {
			if (_videoTick == pb.Frame.nTimetick)
				Console.d("SendPBMedia", "__videoTick == pb.Frame.nTimetick");
			_videoTick = pb.Frame.nTimetick;
		} else {
			if (_audioTick == pb.Frame.nTimetick)
				Console.d("SendPBMedia", "_audioTick == pb.Frame.nTimetick");
			_audioTick = pb.Frame.nTimetick;
		}
		byte[] bytes = pb.GetBytes();
		UdpPacket pack = new UdpPacket();
		pack.Data = bytes;
		pack.ID = pb.Frame.nTimetick;
		pack.Index = 1;
		pack.PartType = UdpPacketPartType.Complete;

		if (pb.Frame.nIsAudio == 0) {
			pack.PacketType = UdpPacketType.Video;
			ListEx<UdpPacket> list = UdpPacketDepart(pack);
			synchronized (_sock) {
				SendPacket(list);
			}
		} else {
			pack.PacketType = UdpPacketType.Audio;
			synchronized (_sock) {
				SendPacket(pack);
			}
		}
	}

	public void SendPacket(ListEx<UdpPacket> list) {
		for (UdpPacket item : list) {
			SendPacket(item);
		}
	}

	int count = 0;

	public void SendPacket(UdpPacket pack) {

		try {
			byte[] bytes = pack.GetBytes();
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, _addr);
			_sock.send(dp);
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
			if (AppConfig._D)
				throw new RuntimeExceptionEx(e);
		}
	}

	public static ListEx<UdpPacket> UdpPacketDepart(UdpPacket pack) {
		int _MAX = 1400;
		if (pack.PartType != UdpPacketPartType.Complete) {
			throw new RuntimeExceptionEx("pack error");
		}
		ListEx<UdpPacket> list = new ListEx<UdpPacket>();
		ListEx<byte[]> bufs = new ListEx<byte[]>();
		java.io.ByteArrayInputStream ms = new ByteArrayInputStream(pack.Data);

		while (ms.available() > 0) {
			byte[] buf = null;
			if (ms.available() >= _MAX) {
				buf = new byte[_MAX];
			} else {
				buf = new byte[ms.available()];
			}
			ms.read(buf, 0, buf.length);
			bufs.add(buf);
		}
		for (int i = 1; i <= bufs.size(); i++) {
			UdpPacket tempVar = new UdpPacket();
			tempVar.ID = pack.ID;
			tempVar.Index = (byte) (bufs.size() - i + 1);
			tempVar.Data = bufs.get(i - 1);
			tempVar.PacketType = pack.PacketType;
			UdpPacket tp = tempVar;
			if (i == 1) {
				tp.PartType = UdpPacketPartType.First;
			} else if (i == bufs.size()) {
				tp.PartType = UdpPacketPartType.Last;
			} else {
				tp.PartType = UdpPacketPartType.Mid;
			}
			list.add(tp);
		}
		if (list.size() == 1) {
			list.get(0).PartType = UdpPacketPartType.Complete;
		}
		return list;

	}

}
