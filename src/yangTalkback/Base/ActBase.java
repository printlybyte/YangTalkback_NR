package yangTalkback.Base;

import java.io.Serializable;

import AXLib.Model.KeyValue;
import AXLib.Utility.CallBack;
import AXLib.Utility.Event.EventReceiver;
import AXLib.Utility.EventArg;
import AXLib.Utility.IAction;
import AXLib.Utility.ICallback;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.TH;
import AXLib.Utility.THPrint;
import AXLib.Utility.ThreadEx;

import yangTalkback.App.App;
import yangTalkback.Base.Prompt.AlertDialogManage;
import yangTalkback.Base.Prompt.PromptButton;
import yangTalkback.Comm.*;

import android.annotation.SuppressLint;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class ActBase extends ActivityGroup implements Serializable {
	static Thread _uiThread = null;
	static THPrint th_pring = new THPrint();
	static ThreadExceptionHandler _threadExceptionHandler = new ThreadExceptionHandler();
	static {
		TH.getThrowEvent().add(th_pring, "Print");
		TH.getThrowEvent().add(new EventReceiver<Throwable>(new IAction<EventArg<Throwable>>() {

			@Override
			public void invoke(EventArg<Throwable> obj) {
				CLLog.Error("未捕获异常", obj.e);
			}
		}));
		Thread.setDefaultUncaughtExceptionHandler(_threadExceptionHandler);
	}
	private static Handler _handlerCallback = null;// UI同步回调
	private static Object _lockHandler = new Object();// ui同步锁
	protected Loading _loading = null;// 加载处理对象
	protected boolean _isLoading = false;// 当前是否正在加载中
	protected ActBase _parentAct;// 当前父视图对象
	protected ActBase _curAct = this;// 当前视图对象
	public boolean IsFinished = false;// 当前视图是否已经结束
	public boolean IsSubActivity = false;// 当前视图是否为子视图

	protected void onManage() {
		_loading = new Loading(this);
		if (_handlerCallback == null) {
			_handlerCallback = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.obj != null && msg.obj instanceof Runnable) {
						((Runnable) msg.obj).run();
					}
				}
			};
		}
		Object obj = GetActivityExtraValue("_parentActivity");
		if (obj == null)
			App.PushAct(this);
		else
			setParentActivity((ActBase) obj);

	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		onManage();
		super.onCreate(savedInstanceState);
		// App appState = (App) this.getApplication();
		// appState.addActivity(this);
	}

	// 获取子视图
	public View getSubActivityView(ActBase act, Class<?> cls, ListEx<KeyValue<String, Serializable>> extras) {
		Intent intent = new Intent(act, cls);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("_parentActivity", this);
		if (extras != null) {
			for (KeyValue<String, Serializable> item : extras) {
				intent.putExtra(item.Key, item.Value);
			}
		}
		Window widow = this.getLocalActivityManager().startActivity(cls.toString(), intent);
		View view = widow.getDecorView();
		return view;
	}

	// 获取子视图
	public View getSubActivityView(Class<?> cls) {
		return getSubActivityView(this, cls, null);
	}

	// 设置当前视图的父视图
	public void setParentActivity(ActBase act) {
		_parentAct = act;
		IsSubActivity = true;
	}

	// 获取当前子视图的顶级视图
	public ActBase getTopAct() {
		if (this.IsSubActivity && _parentAct != null)
			return _parentAct.getTopAct();
		else
			return this;
	}

	// 同步到UI线程调用
	public void post(final ICallback cb) {
		post(new Runnable() {
			@Override
			public void run() {
				cb.invoke();
			}
		});
	}

	// 同步到UI线程调用
	public void post(Runnable run) {
		if (IsUIThread()) {
			run.run();
		} else {
			synchronized (_lockHandler) {
				Message msg = new Message();
				msg.obj = run;
				_handlerCallback.sendMessage(msg);
			}
		}
	}

	// 获取上一视图传递的参数
	@SuppressWarnings("unchecked")
	public <T> T GetActivityDefaultExtraValue(boolean noExistAlert) {
		Object object = GetActivityDefaultExtraValue();
		if (object != null)
			return (T) object;
		else {
			if (noExistAlert)
				AlertAndOut("参数错误");
			return null;
		}
	}

	// 获取上一视图传递的参数默认值
	public Object GetActivityDefaultExtraValue() {
		return GetActivityExtraValue("_default");
	}

	// 获取上一视图传递的参数
	public Object GetActivityExtraValue(String key) {
		Intent intent = getIntent();
		if (intent == null)
			return null;
		Bundle extras = intent.getExtras();
		if (extras == null)
			return null;

		Object object = extras.get(key);
		return object;
	}

	// 切换到视图
	@Override
	public void startActivity(final Intent intent) {
		if (IsUIThread()) {
			if (this.IsSubActivity) {
				((ActBase) this.getParent()).startActivity(intent);
			} else {
				if (_isLoading)
					CloseLoading();
				super.startActivityForResult(intent, 0x08);
			}
			// super.startActivity(intent);
		} else {
			post(new Runnable() {
				@Override
				public void run() {
					startActivity(intent);
				}
			});
		}
	}

	// 切换到视图
	public void startActivity(final Class<?> cls) {
		startActivity(this, cls);
	}

	// 切换到视图,并传递参数
	public void startActivity(final Class<?> cls, Serializable value) {
		Intent intent = new Intent(this, cls);
		intent.putExtra("_default", value);
		startActivity(intent);
	}

	// 切换到视图
	public void startActivity(final Context ctx, final Class<?> cls) {
		startActivity(new Intent(ctx, cls));

	}

	// 判断当前线程是否为UI线程
	protected boolean IsUIThread() {
		Thread thread = Thread.currentThread();
		return App.UIThread == thread;
	}

	/*
	 * 提示信息并退出当前Act
	 */
	public void AlertAndOut(final String msg) {
		if (!IsFinished) {
			this.Prompt(msg, new IAction<Prompt.PromptButton>() {
				@Override
				public void invoke(PromptButton obj) {
					finish();
				}
			});
		} else {
			this.Alert(msg, true);
		}
	}

	/*
	 * 提示信息并退出APP
	 */
	public void AlertAndExit(final String msg) {

		this.Prompt(msg, new IAction<Prompt.PromptButton>() {
			@Override
			public void invoke(PromptButton obj) {
				App.exit();
			}
		});
	}

	/*
	 * 弹出对话框
	 */
	public void Alert(final String msg) {
		Alert(msg, "提示信息");
	}

	// 弹出提示对话框
	public void Alert(final String msg, boolean closeLoading) {
		if (closeLoading)
			CloseLoading();
		Alert(msg, "提示信息");
	}

	// 弹出提示对话框
	public void Alert(Throwable e) {
		RuntimeExceptionEx re = null;
		Throwable te = e;
		ListEx<RuntimeExceptionEx> list = new ListEx<RuntimeExceptionEx>();
		while (e != null) {
			if (e instanceof RuntimeExceptionEx && ((RuntimeExceptionEx) e).Message != null) {
				list.add((RuntimeExceptionEx) e);
				break;
			} else {
				e = e.getCause();
			}
		}
		if (list.size() > 0)
			Alert(list.get(0).Message);
		else {
			Alert(te.getMessage());
		}
	}

	/*
	 * 弹出对话框
	 */
	public void Alert(final String msg, final String title) {
		if (this.IsSubActivity) {
			this.getTopAct().Alert(msg, title);
			return;
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		post(new Runnable() {
			@Override
			public void run() {
				if (!IsFinished)
					builder.show();
				else {
					App.LastAct.Alert(msg, title);
				}

			}
		});
	}

	// 打开提示对话框
	public void Prompt(String msg, final IAction<PromptButton> onClick) {
		if (this.IsSubActivity) {
			this.getTopAct().Prompt(msg, onClick);
			return;
		}
		Prompt.Open(this, msg, PromptButton.YES, onClick);
	}

	// 打开提示对话框,onClick为按下按钮后的回调
	public AlertDialogManage Prompt(String msg, PromptButton btn, final IAction<PromptButton> onClick) {
		if (this.IsSubActivity) {
			return this.getTopAct().Prompt(msg, btn, onClick);
		}
		if (!IsFinished)
			return Prompt.Open(this, msg, btn, onClick);
		else
			return App.LastAct.Prompt(msg, btn, onClick);
	}

	// 关闭提示对话框
	public void ClosePrompt(AlertDialogManage manage) {
		if (this.IsSubActivity) {
			this.getTopAct().ClosePrompt(manage);
		}
		if (!IsFinished)
			Prompt.Close(manage);
		else
			App.LastAct.ClosePrompt(manage);
	}

	// 显示提示信息
	public void Notice(final String msg) {
		Notice(msg, Toast.LENGTH_SHORT);

	}

	// 显示提示信息,duration为显示时间
	public void Notice(final String msg, final int duration) {
		post(new Runnable() {
			@Override
			public void run() {
				if (!IsFinished)
					Toast.makeText(getBaseContext(), msg, duration).show();
			}
		});
	}

	// 打开loading
	public void OpenLoading(String msg) {
		OpenLoading(msg, true, null);
	}

	// 打开loading. msg为提示信息,canCancel为是否可以取消,cancelCallBack为取消后回调事件
	public void OpenLoading(final String msg, final boolean canCancel, final ICallback cancelCallBack) {
		// 如果当前为子视图则调用其父视图弹出loading
		if (this.IsSubActivity) {
			this.getTopAct().OpenLoading(msg, canCancel, cancelCallBack);
			return;
		}
		// 同步到ui
		post(new Runnable() {
			@Override
			public void run() {
				_isLoading = true;
				if (!IsFinished)
					_loading.Open(msg, canCancel, cancelCallBack);
			}
		});
	}

	// 关闭loading
	public void CloseLoading() {
		// 如果当前为子视图则在父视图中关闭
		if (this.IsSubActivity) {
			this.getTopAct().CloseLoading();
			return;
		}
		// 同步到UI线程
		post(new Runnable() {
			@Override
			public void run() {
				_isLoading = false;
				if (!IsFinished)
					_loading.Close();
			}
		});
	}

	// 使用新线程调用传入的方法
	public Thread CallByNewThread(String method) {
		Thread thread = ThreadEx.GetThreadHandle(new CallBack(this, method));
		thread.start();
		return thread;
	}

	// 当按下按键后调用
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			result = result && OnKeyDown_Back();
		}
		return result && super.onKeyDown(keyCode, event);
	}

	// 当按下后退键 后调用
	public boolean OnKeyDown_Back() {
		if (IsSubActivity)
			return false;
		return true;
	}

	// 当结束当前视图
	@Override
	public void finish() {
		CloseLoading();
		IsFinished = true;
		if (!IsSubActivity)
			App.popAct(this);
		super.finish();

	}
}
