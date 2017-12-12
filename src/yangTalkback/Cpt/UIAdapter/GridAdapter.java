package yangTalkback.Cpt.UIAdapter;

 
import yangTalkback.Base.ActBase;

import android.widget.GridView;

public abstract class GridAdapter<T> extends BaseAdapterEx<T> {

	private GridView _gvGrid;

	public GridAdapter(ActBase act, IItemViewCreater itemViewCreater, IDataSource<T> dataSource, GridView gvGrid) {
		super(act, itemViewCreater, dataSource);

		gvGrid.setAdapter(this);
		gvGrid.setOnScrollListener(this);
		gvGrid.setOnItemClickListener(this);
		_gvGrid = gvGrid;
	}

}
