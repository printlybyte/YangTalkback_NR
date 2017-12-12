//package UnitTest;
//
//import java.io.*;
//
//import android.media.CamcorderProfile;
//
//import AXLib.Utility.JNIopenssl;
//import AXLib.Utility.JSONHelper;
//import AXVChat.Codec.FFCodec.AVCodecCfg;
//import AXVChat.Codec.FFCodec.FFCode;
// 
//
//public class TMain {
//	public static void main(String[] args) throws Exception {
//	
//		
//		AVCodecCfg cfg=AVCodecCfg.CreateVideo(352, 288,FFCode.CODEC_ID_H264,96000);
//		cfg.getBytes();
//		System.out.println("----");
//		JNIopenssl.addDir("d:\\环境\\desk\\CJJ\\Debug");
//		//TFFJni.Test();//ffjni接口
//		TMp4P.Test();
////		FFJni.libType=1;
////		FFJni.loadLib();
////		FFJni.Test();
//
//	}
//}