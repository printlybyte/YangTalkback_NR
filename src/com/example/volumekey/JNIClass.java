package com.example.volumekey;

public class JNIClass {
	
	public static native int init(int id);
	public static native int isKeyDown(int fd);
	public static native void unInit(int fd);
	
	static {
		
		System.loadLibrary("jni");
	}
	

}
