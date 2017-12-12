package yangTalkback.Module;

import java.io.Serializable;

import yangTalkback.Act.AppStatusService;
import yangTalkback.Base.ActCLBase;

public class NotifyManage {

	private static AppStatusService gets() {
		return AppStatusService.Instance;
	}

	public static void notify(String msg, String title, Serializable param, ActCLBase act, Class<?> cls) {
		if (gets() != null)
			gets().showNotification(msg, title, param, act, cls);
	}

	public static void cancel() {
		if (gets() != null)
			gets().cancelNotification();
	}

	public static void cancel(String title) {
		if (gets() != null)
			gets().cancelNotification(title);
	}
}
