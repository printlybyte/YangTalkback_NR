package yangTalkback.Comm;

/** 实时对讲状态 */
public enum TalkbackStatus {
	/** 空闲 */
	Idle(0),
	/** 退出中 */
	Leaveing(1),
	/** 进入中 */
	Entering(2),
	/** 对讲中 */
	Talkbacking(3),
	/** 断开 */
	Break(4);

	private int intValue;
	private static java.util.HashMap<Integer, TalkbackStatus> mappings;

	private synchronized static java.util.HashMap<Integer, TalkbackStatus> getMappings() {
		if (mappings == null) {
			mappings = new java.util.HashMap<Integer, TalkbackStatus>();
		}
		return mappings;
	}

	private TalkbackStatus(int value) {
		intValue = value;
		TalkbackStatus.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static TalkbackStatus forValue(int value) {
		return getMappings().get(value);
	}
}