package yangTalkback.Base;

import yangTalkback.Cpt.UIAdapter.IDataSource;
import android.os.Bundle;
import android.view.LayoutInflater;
import AXLib.Utility.ListEx;
import AXLib.Utility.Ex.StringEx;
//需要数据绑定的界面视图
public class ActDBBase extends ActCLBase {
	protected LayoutInflater _inflater;
	protected ActCLBase _act;
	protected int itemWidth = -1, itemHeight = -1;
	protected String _search;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_act = this;
		_inflater = _act.getLayoutInflater();
		_search = (String) GetActivityExtraValue("search");
		if (!StringEx.isEmpty(_search))
			Notice("搜索：" + _search);
	}

	public static abstract class DataSourceBase<T> implements IDataSource<T> {
		protected int _curPageIndex = 1;
		protected ListEx<T> _list = new ListEx<T>();
		protected boolean _isEnd = false;

		public void Reflash() {
			_isEnd = false;
			_curPageIndex = 1;
			_list.clear();
			LoadFirstPage();
		}

		public void LoadFirstPage() {
			_list = nextPage();
		}

		@Override
		public abstract ListEx<T> nextPage();

		@Override
		public boolean isEnd() {
			return _isEnd;
		}

		@Override
		public int getCount() {
			return _list.size();
		}

		@Override
		public T getItem(int index) {
			return _list.get(index);
		}

	}
}
