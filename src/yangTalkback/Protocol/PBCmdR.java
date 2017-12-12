package yangTalkback.Protocol;

public class PBCmdR extends PBCmdC {
	public PBCmdR() {
	}

	public PBCmdR(PBCmdC pb, String json) {
		this.Cmd = pb.Cmd;
		this.JSON = json;
		this.To = pb.From;
		this.From = pb.To;
	}
}