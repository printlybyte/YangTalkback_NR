package yangTalkback.Cpt.Dialog;

import yangTalkback.Base.ActCLBase;
import yangTalkback.Cpt.IContainer;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import AXLib.Utility.Event;
import AXLib.Utility.Ex.StringEx;

public abstract class dlgBase extends Dialog {
	protected final ActCLBase _act;
	protected IContainer _Container;
	protected boolean _ContainerSeted = false;
	public final Event<Object> SubmitEvent = new Event<Object>();
	public final Event<Object> CancelEvent = new Event<Object>();

	public dlgBase(ActCLBase act, String title) {
		super(act);
		_act = act;
		init(act, title);
	}

	public dlgBase(ActCLBase act, String title, int resId) {
		super(act, resId);
		_act = act;
		init(act, title);
	}

	protected void init(ActCLBase act, String title) {
		if (!StringEx.isEmpty(title))
			this.setTitle(title);

		_Container = GetContainer();
		this.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				OnCancel(null);
			}
		});
	}

	protected void OnSubmit(Object obj) {
		SubmitEvent.Trigger(this, obj);
	}

	protected void OnCancel(Object obj) {
		CancelEvent.Trigger(this, null);
	}

	@Override
	public void setContentView(int layoutResID) {
		View v = View.inflate(_act, layoutResID, null);
		setContentView(v);
	}
	

	@Override
	public void addContentView(View view, LayoutParams params) {
		super.addContentView(view, params);
		if (!_ContainerSeted) {
			_Container.SetView(view);
			_ContainerSeted = true;
		}
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		if (!_ContainerSeted) {
			_Container.SetView(view);
			_ContainerSeted = true;
		}
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		if (!_ContainerSeted) {
			_Container.SetView(view);
			_ContainerSeted = true;
		}
	}

	protected abstract IContainer GetContainer();

}
