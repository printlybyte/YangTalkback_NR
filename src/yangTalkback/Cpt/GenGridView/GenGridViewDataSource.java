package yangTalkback.Cpt.GenGridView;

import AXLib.Utility.ListEx;
import yangTalkback.Base.ActDBBase.DataSourceBase;
import yangTalkback.Cpt.UIAdapter.IDataSource;

/** 通用GridView数据源 **/
public class GenGridViewDataSource<T> extends DataSourceBase<T> implements IDataSource<T> {
	private ActGetDataViewActivity<T> _act = null;

	public GenGridViewDataSource(ActGetDataViewActivity<T> act) {
		_act = act;
		LoadFirstPage();
	}

	/** 刷新 **/
	public void Reflash() {
		_isEnd = false;
		_curPageIndex = 1;
		_list.clear();
		LoadFirstPage();
	}

	/** 加载第一页数据 **/
	public void LoadFirstPage() {
		ListEx<T> list = nextPage();
		if (list.size() == 0)
			_act.Notice("未找到相应的数据。");
	}

	/** 获取下一页数据 **/
	@Override
	public ListEx<T> nextPage() {
		ListEx<T> list = _act.getData(_curPageIndex++);

		if (list != null) {
			_list.addAll(list);

			if (list.size() == 0)
				_isEnd = true;
		} else {
			list = new ListEx<T>();
		}
		return list;
	}
}
