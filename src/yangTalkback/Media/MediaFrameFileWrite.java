package yangTalkback.Media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import AXLib.Utility.LittleEndianDataOutputStream;

public class MediaFrameFileWrite {
	FileOutputStream foutStream = null;
	LittleEndianDataOutputStream outStream = null;

	public MediaFrameFileWrite(String fileName) {
		File file = new File(fileName);
		if (file.exists())
			file.delete();
		try {
			foutStream = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		outStream = new LittleEndianDataOutputStream(foutStream);
	}

	public void Write(MediaFrame frame) throws Exception {
		if (outStream == null)
			throw new IllegalStateException();
		byte[] data = frame.getBytes();
		Write(data);
	}

	public void Write(byte[] data) throws IOException {
		synchronized (this) {
			outStream.writeInt(data.length);
			outStream.write(data);
		}
	}

	public void Finish() {
		try {
			outStream.flush();
			outStream.close();
			outStream = null;
			foutStream.close();
			foutStream = null;

		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}
}
