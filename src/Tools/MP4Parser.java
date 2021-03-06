package Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import yangTalkback.Comm.*;
import android.util.Base64;

/**
 * Parse an mp4 file An mp4 file contains a tree where each node has a name and
 * a size This class is used by H264Stream.java to determine the SPS and PPS
 * parameters of a short video recorded by the phone
 */
public class MP4Parser {

	private static final String TAG = "MP4Parser";

	private HashMap<String, Long> boxes = new HashMap<String, Long>();
	private final RandomAccessFile file;
	private long pos = 0;
	private byte[] buffer = new byte[8];

	public MP4Parser(final String path) throws IOException, FileNotFoundException {
		this(new File(path));
		// this.file = new RandomAccessFile(new File(path), "r");
	}

	public MP4Parser(File file) throws IOException, FileNotFoundException {
		this.file = new RandomAccessFile(file, "r");
	}

	/** Parses the mp4 file **/
	public void parse() throws IOException {
		long length = 0;
		try {
			length = file.length();
		} catch (IOException e) {
			throw new IOException("Wrong size");
		}

		try {
			parse("", length);
		} catch (IOException e) {
			CLLog.Warn(e);
			// throw new IOException("Parse error: malformed mp4 file");
		}
	}

	/** Close the file opened when creating the MP4Parser **/
	public void close() {
		try {
			file.close();
		} catch (IOException ignore) {
		}
	}

	public long getBoxPos(String box) throws IOException {
		Long r = boxes.get(box);

		if (r == null)
			return 0;
		return boxes.get(box);
	}

	public StsdBox getStsdBox() throws IOException {
		long index = 0;
		try {
			index = getBoxPos("/moov/trak/mdia/minf/stbl/stsd");
		} catch (Exception e) {
			CLLog.Warn(e);
		}
		try {
			return new StsdBox(file, index);
		} catch (Exception e) {
			throw new IOException("stsd box could not be found");
		}
	}
	private void parse(String path, long len) throws IOException {
		String name = "";
		long sum = 0, newlen = 0;

		boxes.put(path, pos - 8);

		while (sum < len) {

			file.read(buffer, 0, 8);
			sum += 8;
			pos += 8;
			if (validBoxName()) {

				newlen = (buffer[3] & 0xFF | (buffer[2] & 0xFF) << 8 | (buffer[1] & 0xFF) << 16 | (buffer[0] & 0xFF) << 24) - 8;
				// 1061109559+8 correspond to "????" in ASCII the HTC Desire S
				// seems to write that sometimes, maybe other phones do
				if (newlen <= 0 || newlen == 1061109559) {

					return;

				}
				name = new String(buffer, 4, 4);
				// Log.d(TAG,"Atom -> name: "+name+" newlen: "+newlen+" pos: "+pos);
				sum += newlen;
				parse(path + '/' + name, newlen);

			} else {
				if (len < 8) {
					file.seek(file.getFilePointer() - 8 + len);
					sum += len - 8;
				} else {
					if (file.skipBytes((int) (len - 8)) < len - 8) {
						throw new IOException();
					}
					pos += len - 8;
					sum += len - 8;
				}
			}
		}
	}

	private boolean validBoxName() {
		for (int i = 0; i < 4; i++) {
			if ((buffer[i + 4] < 97 || buffer[i + 4] > 122) && (buffer[i + 4] < 48 || buffer[i + 4] > 57))
				return false;
		}
		return true;
	}

}

class StsdBox {

	private RandomAccessFile fis;
	private byte[] buffer = new byte[4];
	private long pos = 0;

	private byte[] pps;
	private byte[] sps;
	private int spsLength, ppsLength;

	/**
	 * Parse the sdsd box in an mp4 file fis: proper mp4 file pos: stsd box's
	 * position in the file
	 */
	public StsdBox(RandomAccessFile fis, long pos) {

		this.fis = fis;
		this.pos = pos;

		findBoxAvcc();
		findSPSandPPS();

	}

	public String getProfileLevel() {
		return toHexString(sps, 1, 3);
	}

	public String getB64PPS() {
		return Base64.encodeToString(pps, 0, ppsLength, Base64.NO_WRAP);
	}

	public String getB64SPS() {
		return Base64.encodeToString(sps, 0, spsLength, Base64.NO_WRAP);
	}

	public byte[] getPPS() {
		return pps;
	}

	public byte[] getSPS() {
		return sps;
	}

	private boolean findSPSandPPS() {
		/*
		 * SPS and PPS parameters are stored in the avcC box You may find really
		 * useful information about this box in the document ISO-IEC 14496-15,
		 * part 5.2.4.1.1 The box's structure is described there
		 * 
		 * aligned(8) class AVCDecoderConfigurationRecord { unsigned int(8)
		 * configurationVersion = 1; unsigned int(8) AVCProfileIndication;
		 * unsigned int(8) profile_compatibility; unsigned int(8)
		 * AVCLevelIndication; bit(6) reserved = 鈥�11111鈥檅; unsigned int(2)
		 * lengthSizeMinusOne; bit(3) reserved = 鈥�11鈥檅; unsigned int(5)
		 * numOfSequenceParameterSets; for (i=0; i< numOfSequenceParameterSets;
		 * i++) { unsigned int(16) sequenceParameterSetLength ;
		 * bit(8*sequenceParameterSetLength) sequenceParameterSetNALUnit; }
		 * unsigned int(8) numOfPictureParameterSets; for (i=0; i<
		 * numOfPictureParameterSets; i++) { unsigned int(16)
		 * pictureParameterSetLength; bit(8*pictureParameterSetLength)
		 * pictureParameterSetNALUnit; } }
		 */
		try {

			// TODO: Here we assume that numOfSequenceParameterSets = 1,
			// numOfPictureParameterSets = 1 !
			// Here we extract the SPS parameter
			fis.skipBytes(7);
			spsLength = 0xFF & fis.readByte();
			sps = new byte[spsLength];
			fis.read(sps, 0, spsLength);
			// Here we extract the PPS parameter
			fis.skipBytes(2);
			ppsLength = 0xFF & fis.readByte();
			pps = new byte[ppsLength];
			fis.read(pps, 0, ppsLength);

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private boolean findBoxAvcc() {
		try {
			fis.seek(pos + 8);
			while (true) {
				int i = 0;
				while (fis.read() != 'a') {
					int b = fis.read();
					if (b == 'a')
						break;
					if (b == -1)
						i++;
					else
						i = 0;
					if (i > 16)
						return false;
				}

				fis.read(buffer, 0, 3);
				if (buffer[0] == 'v' && buffer[1] == 'c' && buffer[2] == 'C')
					break;
			}
		} catch (IOException e) {
			return false;
		}
		return true;

	}

	public static String toHexString(byte[] buffer, int start, int len) {
		String c;
		StringBuilder s = new StringBuilder();
		for (int i = start; i < start + len; i++) {
			c = Integer.toHexString(buffer[i] & 0xFF);
			s.append(c.length() < 2 ? "0" + c : c);
		}
		return s.toString();
	}

}
