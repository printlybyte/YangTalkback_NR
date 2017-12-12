//package UnitTest;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
//import Tools.MP4Config;
//
//import AXLib.Utility.Console;
//import AXLib.Utility.Ex.H16Str;
//
//public class TMp4P {
//	public static void Test() throws FileNotFoundException, IOException {
//		MP4Config mp4Config = new MP4Config("d:\\»·¾³\\desk\\ds\\ds\\spydroid-test.mp4");
//		String level = mp4Config.getProfileLevel();
//		byte[] ppsBuff = mp4Config.getPPS();
//		byte[] spsBuff = mp4Config.getSPS();
//
//		String ppsString = H16Str.To16Strs(ppsBuff);
//		String pspString = H16Str.To16Strs(spsBuff);
//
//		Console.d("Test", String.format("ppsString:%s   pspString:%s    level:%s", ppsString, pspString, level));
//
//	}
//}
