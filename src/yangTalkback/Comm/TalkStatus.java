package yangTalkback.Comm;

/** 视频通话状态 */
public enum TalkStatus {
	/** 空闲 */
	Idle(0),
	/** 请求中 */
	Requesting(1),
	/** 响应中 */
	Repling(2),
	/** 通话中 */
	Talking(3),
	/** 断开中 */
	Closing(4);

	private int intValue;
	private static java.util.HashMap<Integer, TalkStatus> mappings;

	private synchronized static java.util.HashMap<Integer, TalkStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, TalkStatus>();
		}
		return mappings;
	}

	private TalkStatus(int value) {
		intValue = value;
		TalkStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static TalkStatus forValue(int value) {
		return getMappings().get(value);
	}
}