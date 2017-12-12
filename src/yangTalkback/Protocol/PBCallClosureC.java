 package yangTalkback.Protocol;

public class PBCallClosureC extends PBodyJSON
{
	/** 
	 原因 -1错误引发 0手动关闭，1系统断线
	 
	*/
	public int Cause;
 
	@Override
	public String toString()
	{
		return super.toString() + "  Cause:" + (new Integer(Cause)).toString();

	}
}