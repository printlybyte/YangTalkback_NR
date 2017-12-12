//package UnitTest;
//
//import AXLib.Utility.TH;
//import AXVChat.Codec.FFCodec.AVCodecCfg;
//import AXVChat.Codec.FFCodec.FFCode;
//import AXVChat.Codec.FFCodec.FFCodecType;
//import AXVChat.Codec.FFCodec.FFObj;
// 
//import FFCodec.FFJni;
//
//public class TFFJni {
//	public static void Test(){
//		FFJni.libType=1;
//		FFJni.loadLib();
//		FFObj ffObj=new FFObj(FFCodecType.VideoEncode);
//		 try {
//			 AVCodecCfg cfg=AVCodecCfg.CreateVideo(352, 288,FFCode.CODEC_ID_H264,96000);
//			 ffObj.init(cfg);
//			 ffObj.code(null);
//		} catch (Exception e) {
//			TH.Throw(e);
//		}
//	}
//}
