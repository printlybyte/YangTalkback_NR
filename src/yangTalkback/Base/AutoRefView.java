package yangTalkback.Base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author chaobinw 自动关联视图，必须定义为public变量
 * @AutoRefView(id = R.id.tvMsg) public TextView tvMsg;
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRefView {
	/**
	 * 视图控件id
	 * 
	 * @return
	 */
	int id();

	/**
	 * 自定义控件ID,如果设置该值则绑定控件从该ID的控件的子控件中查找
	 * 
	 * @return
	 */
	int includeid() default -1;

	/**
	 * 绑定的点击事件,事件处理方法必须为public 原型为public void modethName(EventArg<View> arg)
	 * 
	 * @return
	 */
	String click() default "";

	/**
	 * 绑定的触摸事件,事件处理方法必须为public 原型为public void modethName(EventArg<MotionEvent>
	 * arg)
	 * 
	 * @return
	 */
	String touch() default "";

	/**
	 * @see bit list
	 * @see 1b :标题,0有标题,1无标题
	 * @see 2b-3b :方向,0自动，1坚向，2模向
	 * @see 4b :锁屏，0自动，1不允许锁屏
	 * 0000 0000
	 * @return
	 */
	byte layout() default 0x00;

 
}
