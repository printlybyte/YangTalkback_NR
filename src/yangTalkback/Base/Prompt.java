package yangTalkback.Base;

import yangTalkback.Comm.*;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import AXLib.Utility.IAction;
import AXLib.Utility.INumberEnum;
import AXLib.Utility.RuntimeExceptionEx;
//提示对话框封装
public class Prompt {
	static Handler _handler = null;
	static Builder _builder;

	public static void Init() {
		_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					AlertDialogManage manage = (AlertDialogManage) msg.obj;
					Builder builder = manage.Builder;
					if (msg.arg1 == 1) {// =0为打开对话框
						AlertDialog alertDialog = manage.Dialog = builder.create();
						manage.DialogCreated = true;
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					} else if (msg.arg1 == 0) {// =1为关闭对话框
						if (manage.DialogCreated && manage.Dialog != null) {
							AlertDialog alertDialog = manage.Dialog;
							alertDialog.dismiss();
						}
					}
				} catch (Exception e) {
					String stack=RuntimeExceptionEx.GetStackTraceString(e);
					CLLog.Error(e);
				}
			}
		};
	}

	public static AlertDialogManage Open(ActBase act, String msg, PromptButton btn, final IAction<PromptButton> onClick) {
		String title = "系统提示";
		_builder = new AlertDialog.Builder(act).setTitle(title).setMessage(msg);
		_builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (onClick != null)
					onClick.invoke(PromptButton.YES);
			}
		});
		if (btn.intValue >= PromptButton.NO.intValue) {
			_builder.setNegativeButton(" 取 消  ", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (onClick != null)
						onClick.invoke(PromptButton.NO);
				}
			});
		}
		if (btn.intValue >= PromptButton.CANCEL.intValue) {
			_builder.setNeutralButton(" 关 闭  ", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (onClick != null)
						onClick.invoke(PromptButton.CANCEL);
				}
			});
		}
		// AlertDialog alertDialog=_builder.create();
		AlertDialogManage manage = new AlertDialogManage();
		manage.Builder = _builder;
		manage.DialogCreated = false;

		Message message = new Message();
		message.obj = manage;
		message.arg1 = 1;
		_handler.sendMessage(message);
		return manage;
	}

	public static void Close(AlertDialogManage manage) {
		Message message = new Message();
		message.obj = manage;
		message.arg1 = 0;
		_handler.sendMessage(message);
	}

	public static class AlertDialogManage {
		public Builder Builder;
		public AlertDialog Dialog;
		public boolean DialogCreated;
	}

	public static enum PromptButton implements INumberEnum {
		YES(0x00), NO(0x01), CANCEL(0x02);

		private int intValue;
		private static java.util.HashMap<Integer, PromptButton> mappings;

		private synchronized static java.util.HashMap<Integer, PromptButton> getMappings() {
			if (mappings == null) {
				mappings = new java.util.HashMap<Integer, PromptButton>();
			}
			return mappings;
		}

		private PromptButton(int value) {
			intValue = value;
			PromptButton.getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static PromptButton forValue(int value) {
			return getMappings().get(value);
		}

		@Override
		public int ToValue() {
			return getValue();
		}

	}

	public static enum PromptType implements INumberEnum {
		Alert(0x00), Message(0x01), Error(0x02);

		private int intValue;
		private static java.util.HashMap<Integer, PromptType> mappings;

		private synchronized static java.util.HashMap<Integer, PromptType> getMappings() {
			if (mappings == null) {
				mappings = new java.util.HashMap<Integer, PromptType>();
			}
			return mappings;
		}

		private PromptType(int value) {
			intValue = value;
			PromptType.getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static PromptType forValue(int value) {
			return getMappings().get(value);
		}

		@Override
		public int ToValue() {
			return getValue();
		}

		public String ToMsgType() {
			if (this == Alert)
				return "系统提示";
			if (this == Message)
				return "消息提示";
			if (this == Error)
				return "系统错误";
			return "系统提示";
		}
	}

}
