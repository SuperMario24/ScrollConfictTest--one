# ScrollConfictTest--one
父元素拦截解决滑动冲突

View事件分发机制
1.dispatchTouchEvent(MotionEvent event):返回结果表示是否消耗当前事件（受当前View的onTouchEvent和子元素的dispatchTouchEvent方法的影响）
2.onInterceptTouchEvent(MotionEvent event):判断是否拦截某个事件，询问自己是否要拦截。
3.onTouchEvent（MotionEvent）:处理点击事件，返回结果表示是否消耗当前事件。

伪代码：
public boolean dispatchTouchEvent(MotionEvent event){
    boolean consume = false;
    if(onInterceptTouchEvent(event)){//如果返回true，表示它要拦截该事件，事件就会交给他处理，调用它的onTouchEvent();
         consume = onTouchEvent(event);
    }else {
          consume = child.dispatchTouchEvent(event);//如果不返回true，就会传递给子元素，调用子元素的dispatchTouchEvent方法，如此反复。
    }
    return consume;
}

优先级：
onTouchListener----->onTouchEvent------>onClickListener
1.如果设置了onTouchListener,那么onTouch方法会被回调，事件如何处理看onTouch的返回值，如果返回false,不拦截，调用他的onTouchEvent处理事件，如果返回
true，那么onTouchEvent不会被调用。
2.onTouchEvent返回false，交给上级处理，返回true，拦截，自己处理。


结论：
7.View没有onInterceptTouchEvent方法，一旦有点击事件传给他，它的onTouchEvent就会被调用。
8.View的onTouchEvent默认都会消耗事件（返回true）。除非他是不可点击的（clickable和longClickable同时为false）
11.事件传递过程是由外向内到的，即事件总是先传递给父元素。


事件分发源码解析：
1.Activity.getWindow.getDecorView()可获取底层容器--------->setContentView所设置的View的父容器。
2.onInterceptTouchEvent不是每次都会被调用的，如果想提前处理所有的点击事件，要选择dispatchTouchEvent方法，只有这个方法能确保每次都会调用。

滑动冲突：
一、外部拦截发：
1.父元素在onInterceptTouchEvent的ACTION_MOVE中拦截。交给父元素的onTouchEvent--ACTION_UP处理.
2.父元素在onInterceptTouchEvent中的ACTION_DOWN必须返回false，不拦截，因为一旦拦截了，ACTION_MOVE，ACTION_UP都会交给父元素处理。
3.ACTION_MOVE，如果父容器需要拦截，返回false。
4.ACTION_UP必须返回false

