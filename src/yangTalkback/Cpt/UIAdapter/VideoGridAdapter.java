package yangTalkback.Cpt.UIAdapter;
//package AXVChat.Cpt.UIAdapter;
//
//import android.app.Activity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.GridView;
//import AXLib.Utility.EventArg;
//import AXVChat.Base.ActBase;
//import AXVChat.Comm.IDModel;
//import AXVChat.Cpt.cptIDItem;
//import AXVChat.Cpt.UIAdapter.BaseAdapterEx.IItemViewCreater;
// 
//public class VideoGridAdapter extends GridAdapter<IDModel> {
//	public VideoGridAdapter(ActBase act, IItemViewCreater itemViewCreater, IDataSource<IDModel> dataSource, GridView gv) {
//		super(act, itemViewCreater, dataSource, gv);
//	}
//	public void onItemClick(EventArg<View> arg) {
//		IDModel model = ((cptIDItem) arg.sender).getModel();
//		ItemClickEvent.Trigger(this, model);
//	}
//
//}
