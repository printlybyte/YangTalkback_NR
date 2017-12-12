package yangTalkback.Net.Model;

import yangTalkback.Comm.IDModel;

public class TalkbackStatusInfo {
	public IDModel IDModel;
	/**
	 * 加入状态 -1已离开 0未进入 1对讲中
	 * 
	 * */
	public int JoinStatus;//
	/** 是否讲话中 */
	public boolean IsTalking;

}
