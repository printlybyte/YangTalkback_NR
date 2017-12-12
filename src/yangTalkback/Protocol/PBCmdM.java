package yangTalkback.Protocol;

public class PBCmdM extends PBCmdC {
	public PBCmdM() {
	}

	public PBCmdM(PBCmdC pb, String json) {
		this.Cmd = pb.Cmd;
		this.JSON = json;
		this.To = pb.From;
		this.From = pb.To;
	}
}