package yangTalkback.Cpt.UIAdapter;

import AXLib.Utility.Console;
import AXLib.Utility.EventArg;
 
 
import yangTalkback.Base.ActBase;
import yangTalkback.Comm.IDModel;
import yangTalkback.Cpt.cptIDItem;

import android.view.View;
import android.widget.GridView;

public class IDGridAdapter extends GridAdapter<IDModel> {

 

	public IDGridAdapter(ActBase act, IItemViewCreater itemViewCreater, IDataSource<IDModel> dataSource, GridView gv) {
		super(act, itemViewCreater, dataSource, gv);
 
	}
 

	public void onItemClick(EventArg<View> arg) {
		Console.d("IDGridAdapter", "onItemClick");
		IDModel model = (IDModel) ((cptIDItem) arg.sender).getModel();
		ItemClickEvent.Trigger(arg.sender, model);
	}
	
	
	
	
	
	
}
