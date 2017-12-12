package yangTalkback.Comm;

public enum MonitorPublishStatus {
	/**
	 * 就绪
	 */
	Stop(0),
	/**
	 * 就绪
	 */
	Ready(1),

	/**
	 * 发布中
	 */
	Publishing(2),
	/**
	 * 中断
	 */
	Interrupt(3),

	/**
	 * 断开中
	 */
	Closing(4);

	private int intValue;
	private static java.util.HashMap<Integer, MonitorPublishStatus> mappings;

	private synchronized static java.util.HashMap<Integer, MonitorPublishStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, MonitorPublishStatus>();
		}
		return mappings;
	}

	private MonitorPublishStatus(int value) {
		intValue = value;
		MonitorPublishStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static MonitorPublishStatus forValue(int value) {
		return getMappings().get(value);
	}
}