package yangTalkback.Protocol;

/**
 心跳包
 
*/
public class PBHeart extends PBodyJSON
{
	//视频通话状态
	public int TalkStatus;
	public int MonitorPublishStatus;
	/**实时对讲状态*/
	public int TalkbackStatus;
	@Override
	public String toString()
	{
		return super.toString() + "  TalkbackStatus:" + (new Integer(TalkbackStatus)).toString();

	}
}