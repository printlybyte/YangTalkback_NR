package yangTalkback.Codec.Cfg;

import java.lang.reflect.Type;

import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.H16Str;
import AXLib.Utility.Ex.Config.GeneralConfig;

import com.google.gson.reflect.TypeToken;
//H264配置参数缓存
public class Mp4CfgListSettings {
	static final String SAVE_KEY = "Mp4CfgListSettings";
	static ListEx<Mp4Cfg> list = new ListEx<Mp4Cfg>();
	static {
		String jsonString = GeneralConfig.Instance.Read(SAVE_KEY);
		Type token = (new TypeToken<ListEx<Mp4Cfg>>() {
		}).getType();
		list = JSONHelper.forJSON(jsonString, token);
		// if (cfgs != null)
		// list = new ListEx<Mp4Cfg>(cfgs);
		// else
		// list = new ListEx<Mp4Cfg>();
		if (list == null)
			list = new ListEx<Mp4Cfg>();
		for (Mp4Cfg cfg : list) {
			if (cfg.strPPS != null)
				cfg.PPS = H16Str.To16Bytes(cfg.strPPS);
			if (cfg.strSPS != null)
				cfg.SPS = H16Str.To16Bytes(cfg.strSPS);
		}
	}
	//添加到缓存
	public static void AddMp4Cfg(final Mp4Cfg cfg) {
		if (cfg.PPS == null || cfg.SPS == null || cfg.profileLevel == null)
			throw new RuntimeExceptionEx("cfg.PPS == null || cfg.SPS == null || cfg.profileLevel == null");
		Mp4Cfg _tempCfg = null;
		for (Mp4Cfg item : list) {
			if (item.TestEq(cfg)) {
				_tempCfg = item;
				break;
			}
		}
		if (_tempCfg != null) {
			_tempCfg.PPS = cfg.PPS;
			_tempCfg.SPS = cfg.SPS;
			_tempCfg.strPPS = cfg.strPPS;
			_tempCfg.strSPS = cfg.strSPS;
			_tempCfg.profileLevel = cfg.profileLevel;
		} else {
			list.add(cfg);
		}
		GeneralConfig.Instance.SaveForJSON(SAVE_KEY, list);
	}
	//是否存在缓存
	public static boolean IsExists(final Mp4Cfg cfg) {
		return list.Exists(new Predicate<Mp4Cfg>() {
			@Override
			public boolean Test(Mp4Cfg obj) {
				return obj.TestEq(cfg);
			}
		});
	}
	//尝试充填 PPS SPS
	public static void fillSPSPPS(Mp4Cfg cfg) throws Exception {
		Mp4Cfg _tempCfg = null;
		for (Mp4Cfg item : list) {
			if (item.TestEq(cfg)) {
				_tempCfg = item;
				break;
			}
		}
		if (_tempCfg == null)
			throw new Exception("Mp4Cfg no find");
		cfg.PPS = _tempCfg.PPS;
		cfg.SPS = _tempCfg.SPS;
		cfg.strPPS = _tempCfg.strPPS;
		cfg.strSPS = _tempCfg.strSPS;
		cfg.profileLevel = _tempCfg.profileLevel;
	}
	//添加参数到配置文件
	public static void AddSettingByFile(String fileName, Mp4Cfg cfg) {
		Tools.MP4Config mp4Config = null;
		try {
			mp4Config = new Tools.MP4Config(fileName);
		} catch (Exception e) {
			RuntimeExceptionEx.PrintException(e);
		}
		try {
			Mp4CfgListSettings.AddMp4Cfg(cfg);
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}

	}
}