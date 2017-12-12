package yangTalkback.Media;

import java.io.*;

import yangTalkback.Codec.Cfg.*;
import yangTalkback.Comm.CLLog;
import yangTalkback.Protocol.*;

import AXLib.Model.*;
import AXLib.Utility.*;
import AXLib.Utility.Ex.*;

//媒体帧
public class MediaFrame implements IPacketObject, IByteObj {
	public byte MediaFrameVersion; // 0x00小版本，0x01全版本,0xff为命令（命令的内容见nRawType对应的枚举）
	// / 扩展字段，默认为1
	// / 当MediaFrameVersion 为0x00或0x01，则该字段：0为不可被丢弃(一般只有首帧是不可丢弃的。)，1为可被丢弃,
	// / 当MediaFrameVersion为0xff时，该字段为命令类型，命令包不可丢弃
	public byte nEx = 1;
	public byte nIsKeyFrame; // 是否为关键帧
	public long nTimetick; // 时间辍
	public byte nIsAudio; // 是否为音频,0:视频,1:音频
	public int nSize; // 数据大小,紧跟着该结构后的数据媒体数据
	public int nOffset; // 偏移量

	public int nEncoder;

	public final String getEncodeName() {
		return MediaFrame.GetGeneralEncodecName(this.nEncoder);
	}

	public short nSPSLen;
	public short nPPSLen;
	public int nWidth;
	public int nHeight;
	/**
	 * 采样率,speex一般为8000
	 */
	public int nFrequency;
	/**
	 * 1=单通道，2=双通道,speex编码一般为1
	 */
	public int nChannel;
	/**
	 * 0=8位,1=16位,一般为2
	 */
	public short nAudioFormat;
	/**
	 * 采集大小,speex 一般为160
	 */
	public short nSamples;

	public byte[] Data = new byte[0];

	public MediaFrame() {
	}

	public MediaFrame(byte version) {
		MediaFrameVersion = version;
	}

	public MediaFrame(byte[] buf) {
		SetBytes(buf);
	}

	public final void SetBytes(byte[] bytes) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			LittleEndianDataInputStream intput = new LittleEndianDataInputStream(stream);
			MediaFrameVersion = intput.readByte();
			nEx = intput.readByte();
			nIsKeyFrame = intput.readByte();
			nTimetick = intput.readLong();
			nIsAudio = intput.readByte();
			nSize = intput.readInt();
			nOffset = intput.readInt();
			// autoSetBytes(intput);
			if (MediaFrameVersion == 1) {
				nEncoder = intput.readInt();
				if (nIsAudio == 0) {
					nSPSLen = intput.readShort();
					nPPSLen = intput.readShort();
					nWidth = intput.readInt();
					nHeight = intput.readInt();
				} else {
					nFrequency = intput.readInt();
					nChannel = intput.readInt();
					nAudioFormat = intput.readShort();
					nSamples = intput.readShort();
				}
			}
			intput.skipBytes(nOffset);
			byte[] data = new byte[nSize];
			if (nSize > 0)
				intput.readFully(data, 0, nSize);
			this.Data = data;
		} catch (Exception e) {
			CLLog.Error(e);
			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create("设置MediaFrame出错", e);
		}

	}

	public final byte[] GetBytes() {
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
		DataOutput output = new LittleEndianDataOutputStream(bOutput);
		try {
			output.writeByte(MediaFrameVersion);
			output.writeByte(nEx);
			output.writeByte(nIsKeyFrame);
			output.writeLong(nTimetick);
			output.writeByte(nIsAudio);
			output.writeInt(nSize);
			int _tOffset = nOffset;// 重置偏移量
			nOffset = 0;
			output.writeInt(nOffset);
			// autoGetBytes(output);
			if (MediaFrameVersion == 1) {
				output.writeInt(nEncoder);
				if (nIsAudio == 0) {
					output.writeShort(nSPSLen);
					output.writeShort(nPPSLen);
					output.writeInt(nWidth);
					output.writeInt(nHeight);
				} else {
					output.writeInt(nFrequency);
					output.writeInt(nChannel);
					output.writeShort(nAudioFormat);
					output.writeShort(nSamples);
				}
			}
			output.write(Data, _tOffset, nSize);
			nOffset = _tOffset;
		} catch (Exception e) {
			CLLog.Error(e);
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		byte[] buf = bOutput.toByteArray();
		return buf;
	}

	@Override
	public String toString() {
		if (!this.IsCommandFrame())

			return String.format("nIsAudio:%1$s  nIsKeyFrame:%2$s  nSize:%3$s", nIsAudio, nIsKeyFrame, nSize);
		else {
			return String.format("nIsAudio:%1$s  Command:%2$s", nIsAudio, MediaFrameCommandType.forId(nEx));
		}
	}

	public final byte[] GetSPS() {
		if (this.nIsAudio == 0 && this.nIsKeyFrame == 1) {
			byte[] sps = new byte[nSPSLen];
			System.arraycopy(Data, 4, sps, 0, nSPSLen);
			return sps;
		} else {
			throw new RuntimeException();
		}
	}

	public final byte[] GetPPS() {
		if (this.nIsAudio == 0 && this.nIsKeyFrame == 1) {
			byte[] pps = new byte[nPPSLen];
			System.arraycopy(Data, 4 + nSPSLen + 4, pps, 0, nPPSLen);
			return pps;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public int getSize() {
		int size = 12 + Data.length;
		if (MediaFrameVersion == 1) {
			size += 10;
		}
		return size;
	}

	@Override
	public byte[] getBytes() {
		return GetBytes();
	}

	@Override
	public void setBytes(byte[] bs) {
		SetBytes(bs);

	}

	public byte[] GetFrameData() {

		if (nOffset == 0)
			return this.Data;
		else {
			byte[] r = new byte[this.nSize];
			System.arraycopy(Data, nOffset, r, 0, nSize);
			return r;
		}
	}

	public boolean IsCommandFrame() {
		return MediaFrameVersion == -1;
	}

	public boolean IsAllowDiscard() {
		return !IsCommandFrame() && !((MediaFrameVersion == 0 || MediaFrameVersion == 1) && nEx == 0);
	}

	public MediaFrameCommandType GetCommandType() {
		if (!IsCommandFrame())
			throw RuntimeExceptionEx.Create("非命令帧");
		return MediaFrameCommandType.forId(nEx);

	}

	public static MediaFrame CreateCommandMediaFrame(boolean isAudio, MediaFrameCommandType cmdType) {
		MediaFrame frame = new MediaFrame();
		frame.MediaFrameVersion = -1;
		frame.nIsAudio = (byte) (isAudio ? 1 : 0);
		frame.nEx = (byte) cmdType.getId();
		frame.nIsKeyFrame = 0;
		return frame;
	}

	public static int H264Encoder = GetGeneralEncoder("H264");
	public static int H263Encoder = GetGeneralEncoder("H263");
	public static int SPEXEncoder = GetGeneralEncoder("SPEX");
	public static int FLV1Encoder = GetGeneralEncoder("FLV1");

	public static int GetGeneralEncoder(String name) {
		name = name.toUpperCase();

		byte[] buf = BitConverter.GetBytes(name);
		return BitConverter.ToInt(buf);
	}

	public static String GetGeneralEncodecName(int generalEncoder) {
		byte[] buf = BitConverter.GetBytes(generalEncoder);

		return BitConverter.ToString(buf).toUpperCase();
	}

	public static int GetEncoderByAVCoderID(int code_id) {
		switch (code_id) {
		case CODEC_ID_H264:
			return H264Encoder;
		case CODEC_ID_H263:
			return H263Encoder;
		case CODEC_ID_FLV1:
			return FLV1Encoder;
		case CODEC_ID_XVID:
			return GetGeneralEncoder("XVID");
		case CODEC_ID_MPEG4:
			return GetGeneralEncoder("MP4V");
		}
		return 0;
	}

	public static final int CODEC_TYPE_VIDEO = 0;
	public static final int PIX_FMT_YUV420P = 0;
	public static final int CODEC_TYPE_AUDIO = 1;
	public static final int CODEC_ID_H263 = 5;
	public static final int PIX_FMT_RGB32 = 6;
	public static final int CODEC_ID_MPEG4 = 13;
	public static final int CODEC_ID_FLV1 = 22;
	public static final int CODEC_ID_H264 = 28;
	public static final int CODEC_ID_XVID = 63;
	public static final int CODEC_ID_AAC = 86018;

	public static int ConverterToFFMPEGCoderID(int id) {
		String name = MediaFrame.GetGeneralEncodecName(id);

		if (StringEx.equalsIgnoreCase(name, "H264")) {
			return (int) CODEC_ID_H264;
		}
		if (StringEx.equalsIgnoreCase(name, "H263")) {
			return (int) CODEC_ID_H263;
		}
		if (StringEx.equalsIgnoreCase(name, "FLV1")) {
			return (int) CODEC_ID_FLV1;
		}
		if (StringEx.equalsIgnoreCase(name, "XVID")) {
			return (int) CODEC_ID_XVID;
		}
		if (StringEx.equalsIgnoreCase(name, "MP4V")) {
			return (int) CODEC_ID_MPEG4;
		}
		return -1;
	}

	public static MediaFrame createVideoKeyFrame(VideoEncodeCfg cfg, long timetick, byte[] data, int offset, int size) {
		MediaFrame mFrame = CreateVideoFrame(cfg, timetick, data, offset, size);// new
																				// MediaFrame((byte)
																				// 1);
		mFrame.MediaFrameVersion = 1;
		mFrame.nIsKeyFrame = 1;
		mFrame.nWidth = cfg.width;
		mFrame.nHeight = cfg.height;
		mFrame.nSPSLen = cfg.SPS == null ? 0 : (byte) cfg.SPS.length;
		mFrame.nPPSLen = cfg.PPS == null ? 0 : (byte) cfg.PPS.length;
		mFrame.nEncoder = cfg.encoder;
		return mFrame;
	}

	public static MediaFrame CreateVideoFrame(VideoEncodeCfg cfg, long timetick, byte[] data, int offset, int size) {
		MediaFrame mFrame = new MediaFrame((byte) 0);
		mFrame.nIsAudio = 0;
		mFrame.nIsKeyFrame = 0;
		mFrame.nTimetick = timetick;
		mFrame.nSize = size;
		mFrame.nOffset = offset;
		mFrame.Data = data;
		return mFrame;
	}

	public static MediaFrame CreateAudioKeyFrame(AudioEncodeCfg cfg, long timetick, byte[] data, int offset, int size) {
		MediaFrame mFrame = CreateAudioFrame(cfg, timetick, data, offset, size);// new
																				// MediaFrame((byte)
																				// 1);
		mFrame.MediaFrameVersion = 1;
		mFrame.nIsKeyFrame = 1;
		mFrame.nFrequency = cfg.frequency;
		mFrame.nChannel = cfg.channel;
		mFrame.nAudioFormat = (short) cfg.format;
		mFrame.nSamples = (short) cfg.samples;
		mFrame.nEncoder = cfg.encoder;

		return mFrame;

	}

	public static MediaFrame CreateAudioFrame(AudioEncodeCfg cfg, long timetick, byte[] data, int offset, int size) {
		MediaFrame mFrame = new MediaFrame((byte) 0);
		mFrame.nIsAudio = 1;
		mFrame.nIsKeyFrame = 0;
		mFrame.nTimetick = timetick;
		mFrame.nSize = size;
		mFrame.nOffset = offset;
		mFrame.Data = data;
		return mFrame;
	}

	public static enum MediaFrameCommandType {
		Start((byte) 0x00), Stop((byte) 0x01), Pause((byte) 0x02), Continue((byte) 0x03), Twoway((byte) 0x04);
		private byte id;

		private MediaFrameCommandType(byte id) {
			this.id = id;
		}

		private void setId(byte id) {
			this.id = id;
		}

		public byte getId() {
			return id;
		}

		public static MediaFrameCommandType forId(byte id) {
			for (MediaFrameCommandType type : MediaFrameCommandType.values())
				if (type.getId() == id)
					return type;
			throw new RuntimeException();
		}
	}
}