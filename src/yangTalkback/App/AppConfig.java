package yangTalkback.App;

import java.lang.reflect.Field;

import yangTalkback.Codec.CamVideoCoderProfile;
import yangTalkback.Codec.CamVideoCoderProfile.CameraType;
import yangTalkback.Codec.CamVideoCoderProfile.ResolutionType;

import android.annotation.SuppressLint;

import AXLib.Utility.ISelect;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.Ex.Config.GeneralConfig;

@SuppressLint("NewApi")
public class AppConfig {
	public final static boolean _D = false;
	public final static AppConfig Instance;
	public final static String KEY_APP = "_APPCONFIG_KEY";
	public int ConnectionTimeOut = 1000 * 30;
	public int ConnectWaitTime = 15 * 1000;
	public static int AudioSendQueueMax = 80;
	public static int VideoSendQueueMax = 20;
	public static int ConnectionCommandReturnTimeOut = 30 * 1000;
	public static int ClientHeartTimespace = 5; // 客户端心跳频率（秒）
	public int ClientSelectCameraIndex = 0; // 客户端使用摄像头序号
	public int ServerPort = 6668;
	public CameraType CameraSelected = CameraType.getDefault();

	public int SpeakMode = 0;// 扬声器模式，0外置，1内置
	public int VideoCaptrueStopMode = 0;// 视频采集停止模式
	public int VideoCaptrueRestartMinutes = 5;
 
	public boolean LeaveExitApp = false;
	public int VideoEncodeMode = 0;// 视频编码模式,0为h264,1为jpeg
	public int PicModeQuality = 2;
	public boolean TimeoutReconnect = true;
	public int VideoSizeModel = ResolutionType.VGA.getId();// 0系统默认，其他对应ResolutionType
	// 如果该值为0则自动使用系统最小的分辨率，否则使用指定分辨率
	public int VideoPriorityMode = 1;// 视频优先模式
	public CamVideoCoderProfile CurCamVideoCoderProfile;
	public CamVideoCoderProfile CurBackCamVideoCoderProfile;
	public CamVideoCoderProfile CurFrontCamVideoCoderProfile;
	public boolean IsLoaded = false;

	public String CDKey = null;
	 
	public String WifiSSID = null;
	public String WifiPWD = null;
	public String ServerIP = "";
	public String RemoteServerIP = null;
	public int RemoteServerPort = 6556;
	public boolean UDPMode = false;
	public boolean IsTwowayMode = false;
	public String LoginID = "";
	public String LoginPWD = "";
	public boolean IsRememberPWD = false;
	public boolean IsAutorun=false;
	static {

		Instance = new AppConfig();
		try {
			Load(Instance);
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}

	}

	public static AppConfig GetDefaultAppConfig() {
		return new AppConfig();

	}

	public AppConfig() {

		Init();
	}

	private void Init() {

	}

	public static void Save(final AppConfig ac) {

		ListEx<Field> fields = new ListEx<Field>(ac.getClass().getFields());
		ListEx<ConfigFieldMode> list = fields.Select(new ISelect<Field, ConfigFieldMode>() {
			@Override
			public ConfigFieldMode Select(Field t) {
				return new ConfigFieldMode(t, ac);
			}
		}).Where(new Predicate<AppConfig.ConfigFieldMode>() {
			@Override
			public boolean Test(ConfigFieldMode obj) {
				return !obj.IsFinal && !obj.IsStatic && !StringEx.equals(obj.Name, "Instance") && !StringEx.equals(obj.Name, "IsLoaded");
			}
		});
		GeneralConfig.Instance.SaveForJSON("__appConfig", list);
	}

	public static void Load(final AppConfig ac) {

		ListEx<Field> fields = new ListEx<Field>(ac.getClass().getFields());
		ConfigFieldMode[] modes = GeneralConfig.Instance.ReadForJSON("__appConfig", ConfigFieldMode[].class);
		if (modes == null) {
			Save(ac);
			return;
		}

		ListEx<ConfigFieldMode> list = new ListEx<ConfigFieldMode>(modes);

		for (final Field field : fields) {
			ConfigFieldMode mode = list.FirstOrDefault(new Predicate<AppConfig.ConfigFieldMode>() {
				@Override
				public boolean Test(ConfigFieldMode obj) {
					return StringEx.equals(field.getName(), obj.Name);
				}
			});
			try {
				if (mode != null) {
					Object obj = null;
					if (StringEx.equals(mode.Type, "int")) {
						obj = Integer.parseInt(mode.JSON);
					} else if (StringEx.equals(mode.Type, "boolean")) {
						obj = Boolean.parseBoolean(mode.JSON);
					} else {
						Class<?> type = Class.forName(mode.Type);
						obj = JSONHelper.forJSON(mode.JSON, type);
					}
					field.set(ac, obj);
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);
			}
		}
		ac.IsLoaded = true;
		ac.Init();
	}

	public void Save() {
		Save(this);
	}

	public void Load() {
		Load(this);

	}

	public void Default() {
		AppConfig ac = GetDefaultAppConfig();
		Load(ac);
		Save();
	}

	public CamVideoCoderProfile GetCamVideoCoderProfile() {
		if (CurCamVideoCoderProfile == null)
			CurCamVideoCoderProfile = CamVideoCoderProfile.GetSystemDefault_CamVideoCoderProfile();
		return CurCamVideoCoderProfile;
	}

	public CamVideoCoderProfile GetCamVideoCoderProfile(CameraType ct) {
		if (ct == CameraType.BackCamera) {
			if (CurBackCamVideoCoderProfile == null)
				return CamVideoCoderProfile.GetSystemDefault_CamVideoCoderProfile(ct);
			else
				return CurBackCamVideoCoderProfile;
		}
		if (ct == CameraType.FrontCamera) {
			if (CurFrontCamVideoCoderProfile == null)
				return CamVideoCoderProfile.GetSystemDefault_CamVideoCoderProfile(ct);
			else
				return CurFrontCamVideoCoderProfile;
		}
		if (CurCamVideoCoderProfile == null)
			CurCamVideoCoderProfile = CamVideoCoderProfile.GetSystemDefault_CamVideoCoderProfile();
		return CurCamVideoCoderProfile;
	}

	public void SetCamVideoCoderProfile(CamVideoCoderProfile value) {
		CurCamVideoCoderProfile = value;
		if (value.HardwareCameraId == CameraType.BackCamera.toHardwareCameraId())
			CurBackCamVideoCoderProfile = value;
		if (value.HardwareCameraId == CameraType.FrontCamera.toHardwareCameraId())
			CurFrontCamVideoCoderProfile = value;
	}

	private static class ConfigFieldMode {
		public String JSON;
		public String Name;
		public String Type;
		public boolean IsFinal;
		public boolean IsStatic;

		public ConfigFieldMode() {
		}

		public ConfigFieldMode(Field field, Object obj) {
			try {
				int modifier = field.getModifiers();
				IsFinal = (modifier & java.lang.reflect.Modifier.FINAL) == java.lang.reflect.Modifier.FINAL;
				IsStatic = (modifier & java.lang.reflect.Modifier.FINAL) == java.lang.reflect.Modifier.STATIC;
				this.Name = field.getName();
				this.Type = field.getType().getName();
				Object value = field.get(obj);
				if (value != null) {
					JSON = JSONHelper.toJSON(value);
				}
			} catch (Exception e) {
				String stack = RuntimeExceptionEx.GetStackTraceString(e);
				throw RuntimeExceptionEx.Create(e);
			}
		}
	}

}
