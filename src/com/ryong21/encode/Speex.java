package com.ryong21.encode;

import yangTalkback.Comm.*;
import AXLib.Utility.RuntimeExceptionEx;

public class Speex {

	/*
	 * quality 1 : 4kbps (very noticeable artifacts, usually intelligible) 2 :
	 * 6kbps (very noticeable artifacts, good intelligibility) 4 : 8kbps
	 * (noticeable artifacts sometimes) 6 : 11kpbs (artifacts usually only
	 * noticeable with headphones) 8 : 15kbps (artifacts not usually noticeable)
	 */
	private static final int DEFAULT_COMPRESSION = 4;
	public int pSpx = 0;

	// private Logger log = LoggerFactory.getLogger(Speex.class);

	public Speex() {
		init();

	}

	public Speex(int compression) {
		init(compression);
	}

	public void init() {
		init(DEFAULT_COMPRESSION);
	}

	public void init(int compression) {
		load();
		try {
			pSpx = open(compression);
			if (pSpx != 0) {
				Denoise(pSpx, -25);
				AGC(pSpx, 24000);
				VAD(pSpx, 70, 65);
			}
		} catch (Throwable e) {
			CLLog.Error("speex≥ı ºªØ ß∞‹", e);

			String stackString = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
		// preprocess();
	}

	static int libType = 0;
	static boolean libLoaded = false;

	private void load() {
		if (libLoaded)
			return;
		try {
			if (libType == 0) {
				System.loadLibrary("speex");
			}
			libLoaded = true;
		} catch (Throwable e) {
			CLLog.Error("speex¿‡ø‚º”‘ÿ ß∞‹", e);
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}

	}

	public native int open(int compression);

	public native void close(int pSpx);

	public native void Denoise(int pSpx, int noiseSuppress);

	public native void AGC(int pSpx, float level);

	public native void VAD(int pSpx, int vadProbStart, int vadProbContinue);

	public native int encode(int pSpx, short lin[], int offset, byte encoded[], int size);

	public native int decode(int pSpx, byte encoded[], short lin[], int size);

	public native int encode(int pSpx, byte lin[], int offset, byte encoded[], int size);

	public native void EchoCancellation(int pSpx, short play[], short mic[], short output[]);

	public native void EchoCancellation(int pSpx, byte play[], byte mic[], byte output[]);

	public native void EchoCapture(int pSpx, short mic[], short revert[]);

	public native void EchoCapture(int pSpx, byte mic[], byte revert[]);

	public native void EchoPlayback(int pSpx, short play[]);

	public native void EchoPlayback(int pSpx, byte play[]);

}
