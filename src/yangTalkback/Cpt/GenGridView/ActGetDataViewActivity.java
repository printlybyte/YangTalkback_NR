package yangTalkback.Cpt.GenGridView;

import yangTalkback.Base.ActDBBase;
import yangTalkback.Cpt.UIAdapter.BaseAdapterEx.IItemViewCreater;

import android.view.View;
import android.view.ViewGroup;

import AXLib.Utility.EventArg;
import AXLib.Utility.ListEx;

/** 通用GridView界面 **/
public abstract class ActGetDataViewActivity<T> extends ActDBBase implements IItemViewCreater, IGenGridViewDataInterface<T> {
	/** 获取数据视图 **/
	public abstract View getItemView(int position, View convertView, ViewGroup parent);

	/** 获取分页数据 **/
	public abstract ListEx<T> getData(int pageIndex);

	/** 列表中按钮点击事件 **/
	public abstract void ItemClickEvent(EventArg<T> arg);

	/** 初始化列表 **/
	protected abstract void InitGridView();

}
