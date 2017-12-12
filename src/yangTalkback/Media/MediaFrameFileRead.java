package yangTalkback.Media;

import java.io.*;

import AXLib.Utility.*;
 

public class MediaFrameFileRead {
	FileInputStream finStream = null;
	LittleEndianDataInputStream inStream = null;

	public MediaFrameFileRead(String fileName) {
		File file = new File(fileName);
		try {
			finStream = new FileInputStream(file);
			inStream = new LittleEndianDataInputStream(finStream);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

	}

	public MediaFrame Read() throws Exception {
		if (inStream.available() <= 4)
			return null;
		int len = inStream.readInt();
		byte[] buf = inStream.readFully(len);
		if (buf.length < len)
			return null;
		MediaFrame f = new MediaFrame((byte) 0);
		f.setBytes(buf);
		return f;
	}

	public void Finish() {
		try {
			inStream.close();
			inStream = null;
			finStream.close();
			finStream = null;
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}
}
