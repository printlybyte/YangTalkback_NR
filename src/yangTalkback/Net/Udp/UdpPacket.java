package yangTalkback.Net.Udp;

import android.annotation.SuppressLint;

import java.util.Arrays;

import AXLib.Utility.BitConverter;
import yangTalkback.Protocol.IPacketObject;

public class UdpPacket implements IPacketObject {

	public UdpPacketPartType PartType = UdpPacketPartType.forValue(0);
	public long ID;
	public byte Index;
	public byte[] Data;
	public UdpPacketType PacketType = UdpPacketType.forValue(0);

	public final byte[] GetBytes() {
		byte[] result = new byte[Data.length + 10];
		result[0] = (byte) (((PartType.getValue()) << 6) | ((PacketType.getValue()) << 4));

		byte[] idBuf = BitConverter.GetBytes(ID);
		result[1] = idBuf[0];
		result[2] = idBuf[1];
		result[3] = idBuf[2];
		result[4] = idBuf[3];
		result[5] = idBuf[4];
		result[6] = idBuf[5];
		result[7] = idBuf[6];
		result[8] = idBuf[7];
		result[9] = (byte) Index;
		System.arraycopy(Data, 0, result, 10, Data.length);
		return result;
	}

	@SuppressLint("NewApi")
	public final void SetBytes(byte[] buf) {

		PartType = UdpPacketPartType.forValue((buf[0] & 0xC0 >> 6));
		PacketType = UdpPacketType.forValue((buf[0] & 0x30 >> 4));
		byte[] idBuf = Arrays.copyOfRange(buf, 1, 9);
		ID = BitConverter.ToLong(idBuf);
		Index = buf[9];
		Data = new byte[buf.length - 10];
		System.arraycopy(buf, 10, Data, 0, Data.length);

	}

}