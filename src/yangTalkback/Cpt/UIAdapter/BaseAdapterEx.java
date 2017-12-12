package yangTalkback.Cpt.UIAdapter;

 
import AXLib.Utility.Event;
import AXLib.Utility.ListEx;
import AXLib.Utility.RuntimeExceptionEx;
 
import yangTalkback.Base.ActBase;
import yangTalkback.Comm.CLLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
 
public abstract class BaseAdapterEx<T> extends BaseAdapter implements OnScrollListener, OnItemClickListener {
	protected LayoutInflater _inflater;
	protected ActBase _act;
	protected IDataSource<T> _dataSource;
	protected boolean _pageLoading = false;
	protected int _lastItemIndex = 0;
	protected IItemViewCreater _itemViewCreater = null;
	public final Event<T> ItemClickEvent = new Event<T>();

	public BaseAdapterEx(ActBase act, IItemViewCreater itemViewCreater, IDataSource<T> dataSource) {
		_act = act;
		_itemViewCreater = itemViewCreater;
		_inflater = act.getLayoutInflater();
		_dataSource = dataSource;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return _itemViewCreater.getItemView(position, convertView, parent);
	}

	@Override
	public int getCount() {
		return _dataSource.getCount();
	}

	@Override
	public Object getItem(int position) {
		return _dataSource.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	protected void loadNextPageByThread() {
		if (_pageLoading || _dataSource.isEnd())
			return;
		_pageLoading = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ListEx<T> list = _dataSource.nextPage();
					if (list.size() > 0) {
						_act.post(new Runnable() {
							@Override
							public void run() {
								_pageLoading = false;
								notifyDataSetChanged();
							}
						});
					} else {
						_pageLoading = false;
					}
				} catch (Exception e) {
					_pageLoading = false;
					String stack = RuntimeExceptionEx.GetStackTraceString(e);
					CLLog.Error(e);
				} finally {

					// _pageLoading = false;
				}
			}
		}).start();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		_lastItemIndex = firstVisibleItem + visibleItemCount;
		// if (totalItemCount == firstVisibleItem + visibleItemCount &&
		// visibleItemCount != 0)// 滚到最后
		// loadNextPageByThread();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 滚到最后
		if ((_lastItemIndex == getCount() && _lastItemIndex != 0) && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			loadNextPageByThread();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		T itemT = _dataSource.getItem(position);
		ItemClickEvent.Trigger(this, itemT);
	}

	public static interface IItemViewCreater {
		View getItemView(int position, View convertView, ViewGroup parent);
	}
}
