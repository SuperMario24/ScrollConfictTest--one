# ScrollConfictTest--one

View的事件体系

一.View的基础知识

1.View的位置参数：

View的位置信息主要由它的四个顶点来决定，分别对应View的四个属性：top，left，right，bottom，这些坐标都是相对于父容器来说的，left，top分别对应左上角
的横纵坐标，right，bottom分别对应右下角的横纵坐标。

View的宽高和坐标的关系：

        width = right - left
        height = bottom - top
        
获取View的四个参数：

        left = getLeft();
        right = getRight();
        top = getTop();
        bottom = getBottom();
        
从Android3.0开始，View增加了几个额外的参数：x，y，translationX和translationY，其中x，y是View左上角的坐标，而translationX，translationY是
View左上角相对于父容器的偏移量，默认值是0，几个参数的换算关系如下所示：

        x = left + translationX;
        y = top + translationY;
        
注意：View在平移过程中，top和left表示的是原始左上角的位置信息，其值并不会发生改变，此时变化的是x，y，translationX和translationY这四个参数。



2.MotionEvent 和 TouchSlop

（1）MotionEvent
手指接触屏幕后所产生的一系列事件：MotionEvent.ACTION_DOWN，MotionEvent.ACTION_MOVE，MotionEvent.ACTION_UP。
通过MotionEvent对象，我们可以得到点击事件发生时的x和y坐标。为此，系统提供了两组方法：

    getX/getY:返回相对于当前View左上角的x和y坐标。
    getRawX/getRawY：返回的是相对于手机屏幕左上角的x和y坐标。


（2）TouchSlop
TouchSlop是系统所能识别的出的被认为是滑动的最小距离，这是一个常量，在不同的设备上这个值有可能是不同的，可以通过以下方式获取：

    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();


3.VelocityTracker

VelocityTracker用来计算手指在滑动过程中的速度，它的使用过程如下：

（1）先初始化对象，然后在View的onTouchEvent方法中追踪当前单击事件的速度：

     mVelocityTracker = VelocityTracker.obtain();
     mVelocityTracker.addMovement(event);

（2）当我们想知道当前的滑动速度时，可以采用如下方式来获得当前的滑动速度：

     mVelocityTracker.computeCurrentVelocity(1000);//计算1秒内滑动的速度
     float xVelocity = mVelocityTracker.getXVelocity();//获取x方向的速度
     float yVelocity = mVelocityTracker.getYVelocity();//获取y方向的速度

在这一步中有两点需要注意：1.获取速度之前必须先计算速度，即getXVelocity()，getYVelocity()之前必须先调用computeCurrentVelocity()方法。
2.这里的速度是指一段时间内手指所滑过的像素数，当手机从右往左滑时，水平方向速度即为负值。单位是毫秒。

（3）最后当不需要使用它的时候，需要调用clear方法来重置并回收内存

    mVelocityTracker.clear();
    mVelocityTracker.recycle();
    
    
    
4.GestureDetector



















