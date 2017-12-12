package yangTalkback.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import yangTalkback.Comm.*;

 
import AXLib.Utility.LittleEndianDataOutputStream;
import AXLib.Utility.RuntimeExceptionEx;
//封包对象
public class Packet implements IPacketObject {

	public byte HeadFlag = 0x00;//开始标识
	public MessageType MsgType = MessageType.forValue(0);//封包类型
	public short From;//发起人号码
	public short To;//接收人号码
	public PBodyBase Body;//封包内容
	public byte EndFlag = 0x00;//结束标识

	public Packet() {
	}

	public Packet(MessageType msgType) {
		MsgType = msgType;
	}

	public Packet(MessageType msgType, PBodyBase body) {
		this(msgType);
		this.From = body.From;
		this.To = body.To;
		this.Body = body;
	}

	public <T extends PBodyBase> T GetBody() {
		return (T) Body;
	}
	//获取byte数组
	public byte[] GetBytes() {
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		LittleEndianDataOutputStream bw = new LittleEndianDataOutputStream(ms);
		try {
			bw.writeByte(HeadFlag);
			bw.writeByte((byte) MsgType.getValue());
			bw.writeShort(From);
			bw.writeShort(To);
			byte[] bufBody = Body.GetBytes();
			bw.writeInt(bufBody.length);
			bw.write(bufBody);
			bw.writeByte(EndFlag);
		} catch (IOException e) {
			CLLog.Error(e);
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}

		byte[] buff = ms.toByteArray();
		return buff;
	}
	//充填对象
	public void SetBytes(byte[] buf) {
		throw new RuntimeExceptionEx("not imp");
	}

	@Override
	public String toString() {
		return String.format("From:%1$s To:%2$s MsgType:%3$s %4$s", From, To, MsgType, Body.toString());
	}

}