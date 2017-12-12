package yangTalkback.Codec.FFCodec;
//FFMPEGÖÐ±àÂëÆ÷´úÂë
public enum FFCode{
	PIX_FMT_YUV420P((int) 0),
	PIX_FMT_RGB24((int) 2),
	PIX_FMT_NV21((int) 26),
	
	
	CODEC_TYPE_VIDEO((int) 0),
	CODEC_TYPE_AUDIO((int) 1),
	CODEC_ID_AAC((int) 86018),
	CODEC_ID_H263((int) 5),
	CODEC_ID_FLV1((int) 22),
	CODEC_ID_H264((int) 28);
	private int id;
	private FFCode(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
}