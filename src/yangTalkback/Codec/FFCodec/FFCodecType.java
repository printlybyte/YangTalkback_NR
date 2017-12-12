package yangTalkback.Codec.FFCodec;
//±‡¬Î¿‡–Õ
public enum FFCodecType {
	VideoDecode((int) 0x1),
	VideoEncode((int) 0x2),
	AudioDecode((int) 0x3),
	AudioEncode((int) 0x4);
	private int id;
	private FFCodecType(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
}
