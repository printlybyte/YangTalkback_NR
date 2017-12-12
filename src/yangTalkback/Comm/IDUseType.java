package yangTalkback.Comm;

public enum IDUseType
{
	Center, //ÖÐÐÄ
	Point, //µã
	Terminal; //ÖÕ¶Ë

	public int getValue()
	{
		return this.ordinal();
	}

	public static IDUseType forValue(int value)
	{
		return values()[value];
	}
}