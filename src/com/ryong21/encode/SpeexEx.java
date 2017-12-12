//package com.ryong21.encode;
//
//import AXLib.Utility.JNIopenssl;
//import AXLib.Utility.RuntimeExceptionEx;
//import CL.Module.CLLog;
//
//public class SpeexEx {
//	static int libType = 0;
//	static boolean libLoaded = false;
//	jay.codec.Speex _jspx = null;
//	/*
//	 * quality 1 : 4kbps (very noticeable artifacts, usually intelligible) 2 :
//	 * 6kbps (very noticeable artifacts, good intelligibility) 4 : 8kbps
//	 * (noticeable artifacts sometimes) 6 : 11kpbs (artifacts usually only
//	 * noticeable with headphones) 8 : 15kbps (artifacts not usually noticeable)
//	 */
//	private static final int DEFAULT_COMPRESSION = 4;
//	public int pSpx = 0;
//
//	// private Logger log = LoggerFactory.getLogger(Speex.class);
//
//	public SpeexEx() {
//		init();
//
//	}
//
//	public SpeexEx(int compression) {
//		init(compression);
//	}
//
//	public void init() {
//		init(DEFAULT_COMPRESSION);
//	}
//
//	public void init(int compression) {
//
//		_jspx = new jay.codec.Speex(compression);
//
//	}
//
//	private void load() {
//
//	}
//
//	public int open(int compression) {
//		return _jspx.open(compression);
//	}
//
//	public void close(int pSpx){
//		_jspx.close();
//	} 
//
//	public   void Denoise(int pSpx, int noiseSuppress){
//
//	}
//
//	public   void AGC(int pSpx, float level){
//
//	}
//
//	public   void VAD(int pSpx, int vadProbStart, int vadProbContinue){
//
//	}
//
//	public   int encode(int pSpx, short lin[], int offset, byte encoded[], int size){
//		return _jspx.encode(lin, offset, encoded, size);
//	}
//
//	public   int decode(int pSpx, byte encoded[], short lin[], int size){
//		return _jspx.decode(encoded, lin, size);
//	}
//
//	public   int encode(int pSpx, byte lin[], int offset, byte encoded[], int size){
//		throw RuntimeExceptionEx.Create("sdf");
//	}
//
//	public   void EchoCancellation(int pSpx, short play[], short mic[], short output[]){
//		throw RuntimeExceptionEx.Create("sdf");
//	}
//
//	public   void EchoCancellation(int pSpx, byte play[], byte mic[], byte output[]){
//		throw RuntimeExceptionEx.Create("sdf");
//	}
//
//	public   void EchoCapture(int pSpx, short mic[], short revert[]){
//		_jspx.echocapture(mic, revert);
//	}
//
//	public   void EchoCapture(int pSpx, byte mic[], byte revert[]){
//		throw RuntimeExceptionEx.Create("sdf");
//	}
//
//	public   void EchoPlayback(int pSpx, short play[]){
//		_jspx.echoplayback(play);
//	}
//
//	public   void EchoPlayback(int pSpx, byte play[]){
//		throw RuntimeExceptionEx.Create("sdf");
//	}
//}
