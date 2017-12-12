package Tools;

import java.util.List;

import AXLib.Utility.ThreadEx;
import AXLib.Utility.Ex.StringEx;
import android.R.integer;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnect {
	private Context _context = null;
	public WifiManager wifiManager;

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	public WifiConnect(Context context) {
		this((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
		_context = context;
	}

	// 构造函数
	public WifiConnect(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	// 打开wifi功能
	public boolean OpenWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}

	// 打开wifi功能
	public boolean CloseWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(false);
		}
		return bRet;
	}

	// 提供一个外部接口，传入要连接的无线网
	public boolean Connect(String SSID, String Password, WifiCipherType Type, int timeOut) {
		if (!this.OpenWifi()) {
			return false;
		}
		// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
		// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
		while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}

		if (IsConnected(SSID))
			return true;

		String curSSID = getCurrentSSID();
		if (curSSID != null) {
			WifiConfiguration curConfig = this.IsExsits(curSSID);
			if (curConfig != null)
				wifiManager.disableNetwork(curConfig.networkId);
			else
				wifiManager.disconnect();
		} else {
			wifiManager.disconnect();
		}
		WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
		//
		if (wifiConfig == null) {
			return false;
		}

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		boolean bRet = false;
		if (tempConfig != null)
			bRet = wifiManager.removeNetwork(tempConfig.networkId);

		int netID = wifiManager.addNetwork(wifiConfig);
		bRet = wifiManager.enableNetwork(netID, true);

		if (!bRet)
			return false;

		// 超时设置
		int count = timeOut / 100;
		while (count-- > 0) {
			if (IsConnected(SSID))
				return true;
			ThreadEx.sleep(100);
		}
		return false;
	}

	public boolean IsConnected(String SSID) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!mWiFiNetworkInfo.isConnected())
			return false;

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null)
			return false;
		String ssid = wifiInfo.getSSID();

		if (!StringEx.equalsIgnoreCase("\"" + SSID + "\"", ssid) && !StringEx.equalsIgnoreCase(SSID, ssid))
			return false;

		WifiInfo wi = wifiManager.getConnectionInfo();
		int ip = wi.getIpAddress();

		return ip != 0;
	}

	// 查看以前是否也配置过这个网络
	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		    config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
		    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			config.status = WifiConfiguration.Status.ENABLED;
			 





		} else {
			return null;
		}
		return config;
	}

	// 得到当前接入点的BSSID
	public String getCurrentSSID() {
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null)
			return null;
		String ssid = wifiInfo.getSSID();
		return ssid;
	}
}