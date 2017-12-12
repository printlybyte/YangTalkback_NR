package yangTalkback.Comm;

import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.TimeUtil;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
//ÈÕÖ¾¼ÇÂ¼Æ÷
public class CLLog {
	private static final Logger logger = LoggerFactory.getLogger();

	public static void Error(Throwable e) {
		String stack = RuntimeExceptionEx.GetStackTraceString(e);
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,stack);
		logger.error(str);
	}

	public static void Error(Object message) {
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,message);
		logger.error(str);
	}

	public static void Error(Object message, Throwable e) {
		String stack = RuntimeExceptionEx.GetStackTraceString(e);
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s\r\n%s", date,message,stack);
		logger.error(str);
	}

	public static void Error(Object message, Class<?> clazz) {
		logger.error(message);
	}

	public static void Error(Object message, Throwable e, Class<?> clazz) {
		logger.error(message, e);
	}

	public static void Debug(Throwable e) {
		String stack = RuntimeExceptionEx.GetStackTraceString(e);
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,stack);
		logger.debug(str);
		 
	}

	public static void Debug(Object message) {
	 
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,message);
		logger.debug(str);
	 
	}

//	public static void Debug(Object message, Throwable e) {
//		logger.debug(message, e);
//	}
//
//	public static void Debug(Object message, Class<?> clazz) {
//		logger.debug(message);
//	}
//
//	public static void Debug(Object message, Throwable e, Class<?> clazz) {
//		logger.debug(message, e);
//	}

	public static void Warn(Object message) {
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,message);
		logger.warn(str);
	 
	}

	public static void Warn(Object message, Throwable e) {
		String stack = RuntimeExceptionEx.GetStackTraceString(e);
		String date=TimeUtil.ToString(TimeUtil.GetCurrentUtilDate(), TimeUtil.YYYY_SECOND);
		String str=String.format("[%s]\r\n%s", date,stack);
		logger.warn(str);
 
	}
//
//	public static void Warn(Object message, Class<?> clazz) {
//		logger.warn(message);
//	}
//
//	public static void Warn(Object message, Throwable e, Class<?> clazz) {
//		logger.warn(message, e);
//	}
//
//	public static void Trace(Object message) {
//		logger.trace(message);
//	}
//
//	public static void Trace(Object message, Throwable e) {
//		logger.trace(message, e);
//	}
//
//	public static void Trace(Object message, Class<?> clazz) {
//		logger.trace(message);
//	}
//
//	public static void Trace(Object message, Throwable e, Class<?> clazz) {
//		logger.trace(message, e);
//	}
}
