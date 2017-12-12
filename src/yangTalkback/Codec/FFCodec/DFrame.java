package yangTalkback.Codec.FFCodec;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;

import AXLib.Model.ByteObj;
import AXLib.Model.ByteObjMember;
import AXLib.Utility.RuntimeExceptionEx;

public class DFrame extends ByteObj {

	@ByteObjMember(index = 10)
	public int nRawType;// 原数据类型，0未压缩，1压缩
	@ByteObjMember(index = 20)
	public int nIsKeyFrame; // 是否为关键帧
	@ByteObjMember(index = 30)
	public int nTimetick; // 时间辍
	@ByteObjMember(index = 40)
	public int nIsAudio; // 是否为音频,0:视频,1:音频
	@ByteObjMember(index = 50)
	public int nSize; // 数据大小,紧跟着该结构后的数据媒体数据
	public byte[] Data;

	@Override
	public int getSize() {
		return 4 * 5;
	}

	@Override
	public void setBytes(byte[] bytes) {
		try {
			DataInput intput = createDataInput(bytes);
			nRawType = intput.readInt();
			nIsKeyFrame = intput.readInt();
			nTimetick = intput.readInt();
			nIsAudio = intput.readInt();
			nSize = intput.readInt();
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] getBytes() {

		try {
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			DataOutput output = createDataOutput(bOutput);
			output.writeInt(nRawType);

			output.writeInt(nIsKeyFrame);
			output.writeInt(nTimetick);
			output.writeInt(nIsAudio);
			output.writeInt(nSize);
			return bOutput.toByteArray();
		} catch (Exception e) {
			throw RuntimeExceptionEx.Create(e);
		}
	}

}
