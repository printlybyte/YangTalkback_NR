package yangTalkback.Net.Udp;

public enum UdpPacketType {
	Message(0), Audio(1), Video(2);

	private int intValue;
	private static java.util.HashMap<Integer, UdpPacketType> mappings;

	private synchronized static java.util.HashMap<Integer, UdpPacketType> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, UdpPacketType>();
		}
		return mappings;
	}

	private UdpPacketType(int value) {
		intValue = value;
		UdpPacketType.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static UdpPacketType forValue(int value) {
		return getMappings().get(value);
	}
}