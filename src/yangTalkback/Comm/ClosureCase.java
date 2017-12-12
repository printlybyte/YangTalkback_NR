package yangTalkback.Comm;

//引起通话关闭的原因
public enum ClosureCase
{
	Error(-1), //错误
	OperClose(0), //手动关闭
	OperCancel(1), //手动关闭
	System(2); //系统

	private int intValue;
	private static java.util.HashMap<Integer, ClosureCase> mappings;
	private synchronized static java.util.HashMap<Integer, ClosureCase> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, ClosureCase>();
		}
		return mappings;
	}

	private ClosureCase(int value)
	{
		intValue = value;
		ClosureCase.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ClosureCase forValue(int value)
	{
		return getMappings().get(value);
	}
}