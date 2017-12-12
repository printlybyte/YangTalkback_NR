package yangTalkback.Codec.FFCodec;

import AXLib.Model.*;

public class AVCodecCfg extends ByteObj   {
	
	@ByteObjMember(index=10) public int codec_id;//FFMPEG中编码器ID
	@ByteObjMember(index=20) public int codec_type;//FFMPEG中编码类型
	@ByteObjMember(index=30) public int bit_rate;//比特率
	@ByteObjMember(index=40) public int width;//宽度
	@ByteObjMember(index=50) public int height;//高度
	@ByteObjMember(index=60) public int time_base_den;//帧率
	@ByteObjMember(index=70) public int time_base_num;//帧间
	@ByteObjMember(index=80) public int gop_size;//
	@ByteObjMember(index=90) public int pix_fmt;//图片格式
	@ByteObjMember(index=100) public int max_b_frames;//最大B帧间隔
	@ByteObjMember(index=110) public int sample_rate;//采样
	@ByteObjMember(index=120) public int channels;//通道
    
    
    public AVCodecCfg()
    {
 
    }
 
    @Override
	public int getSize() {
		return 4*12;
	}
 
    public static  AVCodecCfg  CreateAudio(int channels, int sample_rate, FFCode codec_id , int bit_rate)
    {
    	AVCodecCfg r = new AVCodecCfg();
        r.codec_id = codec_id.getId();
        r.codec_type = FFCode.CODEC_TYPE_AUDIO.getId();
        r.bit_rate = bit_rate;
        r.time_base_den = 0;
        r.time_base_num = 0;
        r.gop_size = 0;
        r.pix_fmt = 0;
        r.max_b_frames = 0;
        r.sample_rate = sample_rate;
        r.channels = channels;
        return r;
    }
    public static AVCodecCfg CreateVideo(int width, int height, FFCode codec_id , int bit_rate )
    {
    	AVCodecCfg r = new AVCodecCfg();
        r.codec_id = codec_id.getId();
        r.width = width;
        r.height = height;
        r.codec_type = FFCode.CODEC_TYPE_VIDEO.getId();
        r.bit_rate = bit_rate;
        r.time_base_den = 15;
        r.time_base_num = 2;
        r.gop_size = 12;
        r.pix_fmt = FFCode.PIX_FMT_YUV420P.getId();
        return r;

    }
 
}
