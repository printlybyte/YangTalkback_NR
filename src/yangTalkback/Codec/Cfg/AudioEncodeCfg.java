package yangTalkback.Codec.Cfg;

import yangTalkback.Media.MediaFrame;
import android.media.AudioFormat;
//音频编码参数
public class AudioEncodeCfg extends CodecCfgBase{
	
	public int frequency = 4000;//采样
	public int format = AudioFormat.ENCODING_PCM_16BIT;//位元
	public int channel=2;//通道模式
	public int samples=160;//这个参数设置小了可以降底延迟
	public int keyFrameRate=300;//关键帧间隔
	/* quality
	 * 1 : 4kbps (very noticeable artifacts, usually intelligible)
	 * 2 : 6kbps (very noticeable artifacts, good intelligibility)
	 * 4 : 8kbps (noticeable artifacts sometimes)
	 * 6 : 11kpbs (artifacts usually only noticeable with headphones)
	 * 8 : 15kbps (artifacts not usually noticeable)
	 */
	public int compression=4;
	public static SpeexEncodeCfg GetDefault(){
		SpeexEncodeCfg r=new SpeexEncodeCfg();
		r.SetEncoder("SPEX");
		r.frequency=8000;
		r.format = AudioFormat.ENCODING_PCM_16BIT;
		r.channel=1;
		r.compression=4;
		r.samples=160;
		return r;
	}
	public static SpeexEncodeCfg ConvertAudioEncodeCfg(MediaFrame f){
		SpeexEncodeCfg r=new SpeexEncodeCfg();
		r.SetEncoder("SPEX");
		r.frequency=f.nFrequency;
		r.format = f.nAudioFormat;
		r.channel=f.nChannel;
		r.compression=4;
		r.samples=f.nSamples;
		return r;
	}
}
