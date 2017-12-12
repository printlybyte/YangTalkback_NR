package yangTalkback.Protocol;

//通信消息类型，一般后面C的为调用，R的为返回
public enum MessageType {
	Unknow, Login_C, // 登录
	Login_R, Logout_C, // 登出
	Logout_R, Heart_C, // 心跳
	Heart_R, AllID_C, // 获取所有ID
	AllID_R, Call_C, // 呼叫
	Call_R, CallClosureC, // 关闭呼叫
	CallClosureR, Media, // 媒体数据
	MonitorOpen_C, // 调用开启监控
	MonitorOpen_R, // 返回开启监控
	MonitorClose_C, // 调用关闭监控
	MonitorClose_R, // 返咽关闭监控
	Cmd_C, // 调用命令
	Cmd_R, // 命令返回
	Cmd_M;// 命令通告

	public int getValue() {
		return this.ordinal();
	}

	public static MessageType forValue(int value) {
		return values()[value];
	}
}