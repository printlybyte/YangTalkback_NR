package yangTalkback.Cpt.GenGridView;

import AXLib.Utility.ListEx;
/** 通用GridView数据接口 **/
public interface IGenGridViewDataInterface<T> {
	/** 获取指定页数据 **/
	ListEx<T> getData(int pageIndex);
}