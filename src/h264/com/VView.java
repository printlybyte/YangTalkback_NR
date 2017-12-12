package h264.com;

import AXLib.Utility.RuntimeExceptionEx;

public class VView {
	static {
		try {
			System.loadLibrary("H264Android");
		} catch (Throwable e) {
			String s = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
	}

	public native int InitDecoder(int width, int height);

	public native int UninitDecoder();

	public native int DecoderNal(byte[] in, int insize, byte[] out);
}
