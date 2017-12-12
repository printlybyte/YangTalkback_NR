package yangTalkback.Base;

import java.lang.reflect.Field;

import AXLib.Utility.Event;
import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class ActAutoRefView extends ActBase {
	// 点击事件调用集合
	ListEx<BindEventModel<View>> ClickEvents = new ListEx<BindEventModel<View>>();

	// 触摸事件调用集合
	ListEx<BindEventModel<MotionEvent>> TouchEvents = new ListEx<BindEventModel<MotionEvent>>();

	protected int CurScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RefView_Activity();
	}

	/*
	 * 设置对应视图
	 * 
	 * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int id) {
		super.setContentView(id);
		try {
			RefView_Field();
		} catch (Exception e) {
			String stack = RuntimeExceptionEx.GetStackTraceString(e);
			throw RuntimeExceptionEx.Create(e);
		}
	}

	
	
	
	protected boolean ScreenOrientationIsFit() {
		return true;
 
	}

	public void RefView_Activity() {
		AutoRefView r = this.getClass().getAnnotation(AutoRefView.class);
		if (r != null) {
			int layout = r.layout();
			if ((layout & 0x01) == 0x01)// 标题
				requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(r.id());

			if ((layout & 0x08) == 0x08)
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 取消手机屏幕自动锁屏

		}
	}

	/*
	 * 关联控件
	 */
	public void RefView_Field() {
		// 获取当前类字段成员
		Field[] fs = this.getClass().getFields();
		for (Field f : fs) {
			// 获取字段的Annotation
			AutoRefView r = f.getAnnotation(AutoRefView.class);
			if (r != null) {
				int id = r.id();
				int includeid = r.includeid();
				View v = null;
				if (includeid != -1) {
					View pv = findViewById(includeid);
					v = pv.findViewById(id);
				} else
					v = findViewById(id);
				if (v != null) {

					try {
						if (f.get(this) instanceof yangTalkback.Cpt.IContainer) {
							yangTalkback.Cpt.IContainer ic = ((yangTalkback.Cpt.IContainer) f.get(this));
							ic.SetView(v);

							// 如果设置了点击事件，则绑定点击事件
							if (r.click() != "")
								ic.getClickEvent().add(this, r.click());

							// 如果设置了触摸事件，则绑定触摸事件
							if (r.touch() != "")
								ic.getTouchEvent().add(this, r.touch());

						} else {
							f.set(this, v);// 关联视图

							// 如果设置了点击事件，则绑定点击事件
							if (r.click() != "")
								this.BindClickEvent(v, r.click());
							// 如果设置了触摸事件，则绑定触摸事件
							if (r.touch() != "")
								this.BindTouchEvent(v, r.touch());
						}

					} catch (Exception e) {
						String stack = RuntimeExceptionEx.GetStackTraceString(e);
						String fname = f.getName();
						throw RuntimeExceptionEx.Create(String.format("自动关联视图错误，名称：%s", f.getName()), e);
					}

				}
			}
		}
	}

	/**
	 * 绑定点击事件
	 * 
	 * @param v
	 *            要绑定事件的对象
	 * @param modeth
	 *            接收事件的方法名,方法原型：public void modethName(EventArg<View> arg)
	 */
	public void BindClickEvent(View v, String modeth) {
		BindEventModel<View> model = null;
		for (BindEventModel<View> m : ClickEvents) {
			if (m.getView().equals(v)) {
				model = m;
				break;
			}
		}
		if (model == null) {
			model = new BindEventModel<View>(v);
			ClickEvents.add(model);
			final BindEventModel<View> tModel = model;
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					tModel.getEvent().Trigger(v, v);
				}
			});
		}
		model.getEvent().add(this, modeth);
	}

	/**
	 * 绑定触摸事件
	 * 
	 * @param v
	 *            要绑定事件的对象
	 * @param modeth
	 *            接收事件的方法名,方法原型：public void modethName(EventArg<MotionEvent>
	 *            arg)
	 */
	public void BindTouchEvent(View v, String modeth) {
		BindEventModel<MotionEvent> model = null;
		for (BindEventModel<MotionEvent> m : TouchEvents) {
			if (m.getView().equals(v)) {
				model = m;
				break;
			}
		}
		if (model == null) {
			model = new BindEventModel<MotionEvent>(v);
			TouchEvents.add(model);
			final BindEventModel<MotionEvent> tModel = model;
			v.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					tModel.getEvent().Trigger(v, event);
					return false;
				}
			});
		}
		model.getEvent().add(this, modeth);
	}

	/*
	 * 事件绑定对象
	 */
	protected class BindEventModel<T> {
		private Event<T> event = new Event<T>();
		private View view;

		public BindEventModel(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}

		public Event<T> getEvent() {
			return event;
		}

		public void Call(EventArg<T> arg) {
			event.Trigger(view, arg.e);
		}
	}
}
