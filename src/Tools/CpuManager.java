package Tools;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;

import java.io.FileReader;

import java.io.IOException;

import java.io.InputStream;

 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;

public class CpuManager {

	// 获取CPU最大频率（单位KHZ）

	// "/system/bin/cat" 命令行

	// "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径

	public static String GetBRAND() {
		return Build.BRAND;
	}

	public static String GetBuild() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s======================\r\n", "Build"));
		sb.append(String.format("Build.BOARD // 主板 :							%s\r\n", Build.BOARD));
		sb.append(String.format("Build.BRAND // android系统定制商  :				%s\r\n", Build.BRAND));
		sb.append(String.format("Build.CPU_ABI // cpu指令集  :					%s\r\n", Build.CPU_ABI));
		sb.append(String.format("Build.DEVICE // 设备参数  :						%s\r\n", Build.DEVICE));
		sb.append(String.format("Build.DISPLAY // 显示屏参数  :					%s\r\n", Build.DISPLAY));
		sb.append(String.format("Build.FINGERPRINT // 硬件名称  :					%s\r\n", Build.FINGERPRINT));

		sb.append(String.format("Build.MANUFACTURER // 硬件制造商  :				%s\r\n", Build.MANUFACTURER));
		sb.append(String.format("Build.MODEL // 版本  :							%s\r\n", Build.MODEL));
		sb.append(String.format("Build.PRODUCT // 手机制造商 :					%s\r\n", Build.PRODUCT));
		sb.append(String.format("Build.VERSION.CODENAME  // 当前开发代号  :		%s\r\n", Build.VERSION.CODENAME));
		sb.append(String.format("Build.VERSION.INCREMENTAL  // 源码控制版本号  :	%s\r\n", Build.VERSION.INCREMENTAL));
		sb.append(String.format("Build.VERSION.RELEASE  // 版本字符串  :			%s\r\n", Build.VERSION.RELEASE));
		sb.append(String.format("Build.VERSION.SDK  // 版本号 :					%s\r\n", Build.VERSION.SDK));
		sb.append(String.format("Build.VERSION.SDK_INT // 版本号 :				%s\r\n", Build.VERSION.SDK_INT));
		return sb.toString();
	}

	public static String GetCpu() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s======================\r\n", "CPU"));
		String cn = CpuManager.getCpuName();
		String max = CpuManager.getMaxCpuFreq();
		String min = CpuManager.getMinCpuFreq();
		String cpu = CpuManager.getCurCpuFreq();

		sb.append(String.format("CpuName  	 	:%s\r\n", cn));
		sb.append(String.format("MaxCpuFreq   		:%s\r\n", max));
		sb.append(String.format("MinCpuFreq  		:%s\r\n", min));
		sb.append(String.format("CurCpuFreq   		:%s\r\n", cpu));
		return sb.toString();

	}

	private static String getMaxCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		}
		return result.trim();

	}

	// 获取CPU最小频率（单位KHZ）
	private static String getMinCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		}
		return result.trim();
	}

	// 实时获取CPU当前频率（单位KHZ）

	private static String getCurCpuFreq() {
		String result = "N/A";
		try {
			FileReader fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			result = text.trim();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	// 获取CPU名字

	private static String getCpuName() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			for (int i = 0; i < array.length; i++) {
			}
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void getTotalMemory() {
		String str1 = "/proc/meminfo";
		String str2 = "";
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			while ((str2 = localBufferedReader.readLine()) != null) {
				// Log.i(TAG, "---" + str2);
			}
		} catch (IOException e) {
		}
	}

	public long[] getRomMemroy() {
		long[] romInfo = new long[2];
		// Total rom memory
		romInfo[0] = getTotalInternalMemorySize();

		// Available rom memory
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		romInfo[1] = blockSize * availableBlocks;
		// getVersion();
		return romInfo;
	}

	public long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			// level加%就是当前电量了
		}
	};

	public String[] getVersion() {
		String[] version = { "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[0] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
		}
		version[1] = Build.VERSION.RELEASE;// firmware version
		version[2] = Build.MODEL;// model
		version[3] = Build.DISPLAY;// system version
		return version;
	}
	// public String[] getOtherInfo(){
	// String[] other={"null","null"};
	// WifiManager wifiManager = (WifiManager)
	// mContext.getSystemService(Context.WIFI_SERVICE);
	// WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	// if(wifiInfo.getMacAddress()!=null){
	// other[0]=wifiInfo.getMacAddress();
	// } else {
	// other[0] = "Fail";
	// }
	// // other[1] = getTimes();
	// return other;
	// }
	// private String getTimes() {
	// long ut = SystemClock.elapsedRealtime() / 1000;
	// if (ut == 0) {
	// ut = 1;
	// }
	// int m = (int) ((ut / 60) % 60);
	// int h = (int) ((ut / 3600));
	// return h + " " + mContext.getString(R.string.info_times_hour) + m + " "
	// + mContext.getString(R.string.info_times_minute);
	// }

}
