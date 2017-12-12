package yangTalkback.Protocol;

import AXLib.Utility.BitConverter;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.RuntimeExceptionEx;

public class PBodyJSON extends PBodyBase
{
	public final String ToJson()
	{
		return JSONHelper.toJSON(this);
	}
	public final void ForJson(String json)
	{
		throw new RuntimeExceptionEx("not imp");
	}
	@Override
	public byte[] GetBytes()
	{
		String json = ToJson();
		
		return BitConverter.GetBytes(json);
	}
	@Override
	public void SetBytes(byte[] buf)
	{
		throw new RuntimeExceptionEx("not imp");
	}
}