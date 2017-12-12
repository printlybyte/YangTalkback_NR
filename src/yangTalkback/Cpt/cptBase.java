package yangTalkback.Cpt;

import java.lang.reflect.Field;

import AXLib.Utility.Event;
import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;

import yangTalkback.Base.AutoRefView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public abstract class cptBase implements IContainer {

	private Context _ctx = null;

	// 点击事件调用集合
	ListEx<BindEventModel<View>> ClickEvents = new ListEx<BindEventModel<View>>();

	// 触摸事件调用集合
	ListEx<BindEventModel<MotionEvent>> TouchEvents = new ListEx<BindEventModel<MotionEvent>>();

	// protected SvrProxy _svr = SvrProxy.Instance;

	private boolean _enabled = true;

	public View IncludeView;

	public final Event<View> ClickEvent = new Event<View>();
	public final Event<MotionEvent> TouchEvent = new Event<MotionEvent>();
	public final Event<Object> ExecutionEvent = new Event<Object>();

	public cptBase(Context ctx) {
		_ctx = ctx;
	}

	@Override
	public void SetView(View v) {
		IncludeView = v;
		RefView_Field();
		InitEvent();
	}

	public Event<View> getClickEvent() {
		return ClickEvent;
	}

	public Event<MotionEvent> getTouchEvent() {
		return TouchEvent;
	}

	public Event<Object> getExecutionEvent() {
		return ExecutionEvent;
	}


	private void InitEvent() {
		if (IncludeView != null) {
			IncludeView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					OnClick(v);
				}
			});
		}
	}

	protected void OnClick(View view) {
		if (_enabled) {
			if (ClickEvent.getHandleCount() > 0)
				ClickEvent.Trigger(cptBase.this, view);
		}
	}

	protected void OnTouch(MotionEvent event) {
		if (_enabled) {
			if (TouchEvent.getHandleCount() > 0)
				TouchEvent.Trigger(cptBase.this, event);
		}
	}

	protected void OnExecution(Object obj) {
		if (ExecutionEvent.getHandleCount() > 0)
			ExecutionEvent.Trigger(this, obj);
	}

	public void RefView_Field() {
		// 获取当前类字段成员
		Field[] fs = this.getClass().getFields();
		for (Field f : fs) {
			// 获取字段的Annotation
			AutoRefView r = f.getAnnotation(AutoRefView.class);
			if (r != null) {
				int id = r.id();
				View v = IncludeView.findViewById(id);
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
						throw new RuntimeExceptionEx(String.format("自动关联视图错误，名称：%s", f.getName()), e);
					}

				}
			}
		}
	}

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

	public void setVisibility(int visibility) {
		if (IncludeView != null)
			IncludeView.setVisibility(visibility);
		else
			throw RuntimeExceptionEx.Create("IncludeView is null");
	}

	public int getVisibility() {

		if (IncludeView != null)
			return IncludeView.getVisibility();
		else
			throw RuntimeExceptionEx.Create("IncludeView is null");
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
		IncludeView.setEnabled(enabled);
	}

	public boolean getEnabled() {
		return _enabled;
	}

}
