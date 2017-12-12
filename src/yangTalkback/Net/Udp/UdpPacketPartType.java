package yangTalkback.Net.Udp;


public enum UdpPacketPartType {
	Complete(0), // 完整的媒体包，没有被拆分
	First(1), // 拆分媒体包时第一个封包
	Mid(2), // 拆分媒体包时中间的封包
	Last(3); // 拆分媒体包时最后一个封包

	private int intValue;
	private static java.util.HashMap<Integer, UdpPacketPartType> mappings;

	private synchronized static java.util.HashMap<Integer, UdpPacketPartType> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, UdpPacketPartType>();
		}
		return mappings;
	}

	private UdpPacketPartType(int value) {
		intValue = value;
		UdpPacketPartType.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static UdpPacketPartType forValue(int value) {
		return getMappings().get(value);
	}
}