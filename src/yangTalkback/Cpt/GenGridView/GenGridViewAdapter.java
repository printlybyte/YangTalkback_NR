package yangTalkback.Cpt.GenGridView;

import AXLib.Utility.EventArg;
import AXLib.Utility.RuntimeExceptionEx;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Comm.CLLog;
import yangTalkback.Cpt.UIAdapter.GridAdapter;
import yangTalkback.Cpt.UIAdapter.IDataSource;
import android.view.View;
import android.widget.GridView;

/** 通用GridView数据适配器 **/
public class GenGridViewAdapter<T> extends GridAdapter<T> {
	public GenGridViewAdapter(ActCLBase act, IItemViewCreater itemViewCreater, IDataSource<T> dataSource, GridView gv) {
		super(act, itemViewCreater, dataSource, gv);
	}

	/** 通用GridView数据项点击事件 **/
	public void onItemClick(EventArg<View> arg) {
		if (arg != null && arg.sender != null && arg.sender instanceof IGenGridViewItem) {
			try {
				T model = ((IGenGridViewItem<T>) arg.sender).getModel();

				ItemClickEvent.Trigger(arg.sender, model);

			} catch (Exception e) {
				CLLog.Error(e);
				throw RuntimeExceptionEx.Create(e);
			}
		}
	}
}