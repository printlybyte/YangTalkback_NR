package yangTalkback.Protocol;

public class PBCmdC extends PBodyJSON {
	public String Cmd;
	public String JSON;

	public PBCmdC() {

	}

	public PBCmdC(String cmd, String json) {
		Cmd = cmd;
		JSON = json;
	}

	public PBCmdC(Short from, String cmd, String json) {
		From = from;
		Cmd = cmd;
		JSON = json;
	}

	// TALK_Req 请求对讲
	// TALK_Leave 离开对讲
	// TALK_Enter 进入对讲
	// TALK_Invite 邀请对讲

	/** TALK_Req 请求对讲 */
	public final static String CMD_Type_TALK_Req = "TALK_Req";

	/** TALK_Leave 离开对讲 */
	public final static String CMD_Type_TALK_Leave = "TALK_Leave";

	/** TALK_Enter 进入对讲 */
	public final static String CMD_Type_TALK_Enter = "TALK_Enter";

	/** TALK_Invite 邀请对讲 */
	public final static String CMD_Type_TALK_Invite = "TALK_Invite";

	/** TALK_Info 对讲信息 */
	public final static String CMD_Type_TALK_Info = "TALK_Info";
	
	/** TALK_MyChannel 我的对讲信息 */
	public final static String CMD_Type_TALK_MyChannel = "TALK_MyChannel";
	
	
	/** SetMode 设置对讲模式 true为0为单工，1为双工 */
	public final static String CMD_Type_TALK_SetMode = "TALK_SetMode";
	 
	public final static String CMD_Type_RECORD_List = "RECORD_List";
	public final static String CMD_Type_RECORD_Get = "RECORD_Get";
	
}