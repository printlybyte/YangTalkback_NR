package yangTalkback.Cpt;

import AXLib.Utility.Event;
import android.view.MotionEvent;
import android.view.View;

public interface IContainer {

	void SetView(View v);

	Event<View> getClickEvent();

	Event<MotionEvent> getTouchEvent();

	Event<Object> getExecutionEvent();

 
}
