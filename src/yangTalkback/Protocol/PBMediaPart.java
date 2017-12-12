package yangTalkback.Protocol;

 

public enum PBMediaPart 
{
	Complete,
	First,
	Mid,
	End;

	public byte getValue()
	{
		return (byte)this.ordinal();
	}

	public static PBMediaPart forValue(byte value)
	{
		return values()[value];
	}
}