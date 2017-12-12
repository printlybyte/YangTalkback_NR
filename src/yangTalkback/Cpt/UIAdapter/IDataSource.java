package yangTalkback.Cpt.UIAdapter;

import AXLib.Utility.ListEx;

//绑定到数据集视图的数据源接口
public interface IDataSource<T> {
	//是否到达结尾
	boolean isEnd();
	//获取总数
	int getCount();
	//获取指定项
	T getItem(int index);
	//下一页
	ListEx<T> nextPage();
	//ListEx<T> getPage(int index);

}
