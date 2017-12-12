package yangTalkback.Protocol;

public abstract class PBodyBase implements IPacketObject
{
	public short From;
	public short To;
	public boolean Result;
	public String Message;

	public abstract byte[] GetBytes();
	public abstract void SetBytes(byte[] buf);
	@Override
	public String toString()
	{
		return String.format("Result:%1$s  Message:%2$s",Result, Message);
	}
	public final String ToString1()
	{
		return String.format("From:%1$s  To:%2$s  Result:%3$s  Message:%4$s", From, To, Result, Message);
	}
}