package yangTalkback.Codec.FFCodec;
//package AXVChat.Codec.FFCodec;
//
//import java.io.FileInputStream;
//
//import AXLib.Utility.Console;
//import AXLib.Utility.LittleEndianDataInputStream;
//import AXLib.Utility.LittleEndianDataOutputStream;
//import AXLib.Utility.TH;
////i
//public class TestCodec {
//	
//	public static void Test() throws Exception{
//		int width=352;
//		int height=288;
//		FFCode ffCode=FFCode.CODEC_ID_H264;
//		AVCodecCfg cfgEnc=AVCodecCfg.CreateVideo(width, height, ffCode, 96000);
//		AVCodecCfg cfgDec=AVCodecCfg.CreateVideo(width, height, ffCode, 96000);
//		FFObj ffEnc=new FFObj(FFCodecType.VideoEncode);
//		ffEnc.init(cfgEnc);
//		
//		FileInputStream fis=new FileInputStream("/sdcard/dcim/aa.yuv");
//		LittleEndianDataInputStream dis=new LittleEndianDataInputStream(fis);
//		while(fis.available()>0){
//			int len=dis.readInt();
//			byte[] inBuff=dis.readFully(len);
//			DFrame dFrame=ffEnc.code(inBuff);
//			wf(dFrame);
//		}
//		
//		
//	}
//	static java.io.FileOutputStream fo = null;
//	static LittleEndianDataOutputStream os = null;
//
//	private static void wf(DFrame df) {
//		try {
//			
//			if (fo == null) {
//				fo = new java.io.FileOutputStream("/sdcard/dcim/testEnc.h264", false);
//				os = new LittleEndianDataOutputStream(fo);
//			}
//			os.write(df.Data, 0, df.Data.length);
//			os.flush();
//			Console.d("FileIO", String.format("–¥»Î:%d", df.Data.length));
//		} catch (Exception e) {
//			TH.Throw(e);
//		}
//
//
//	}
//}
