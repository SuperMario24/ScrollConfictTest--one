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

手势检测，用于辅助检测用户的单击，滑动，长按，双击等行为，实际开发中用的较少，这里就不做过多介绍了。



5.Scroller

弹性滑动对象，用于实现View的弹性滑动，Scroller来实现有过渡效果的滑动。Scroller的使用方法：

（1）创建Scroller的实例

       mScroller = new Scroller(context);
       
（2）调用startScroll()方法来初始化滚动数据并刷新界面

        public void smoothScrollTo(int destX,int destY){
                int scrollX = getScrollX();
                int delta = destX - scrollX;
                //1000ms内滑向destX
                mScroller.startScroll(scrollX,0,delta,0,1000);
                nvalidate();
        }

      
       
 （3）重写computeScroll()方法，并在其内部完成平滑滚动的逻辑    
 
          public void computeScroll() {
                // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
                if(mScroller.computeScrollOffset()){
                    scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
                    postInvalidate();
                }
           }


二.View的滑动

通过三种方式可以实现View的滑动，1.通过View本身提供的scrollTo/scrollBy方法。2.通过动画给View施加平移效果。3.通过改变View的LayoutParams
使得View重新布局从而实现滑动。


1.使用scrollTo/scrollBy

         /**
             * Set the scrolled position of your view. This will cause a call to
             * {@link #onScrollChanged(int, int, int, int)} and the view will be
             * invalidated.
             * @param x the x position to scroll to
             * @param y the y position to scroll to
             */
            public void scrollTo(int x, int y) {
                if (mScrollX != x || mScrollY != y) {
                    int oldX = mScrollX;
                    int oldY = mScrollY;
                    mScrollX = x;
                    mScrollY = y;
                    invalidateParentCaches();
                    onScrollChanged(mScrollX, mScrollY, oldX, oldY);
                    if (!awakenScrollBars()) {
                        postInvalidateOnAnimation();
                    }
                }
            }

            /**
             * Move the scrolled position of your view. This will cause a call to
             * {@link #onScrollChanged(int, int, int, int)} and the view will be
             * invalidated.
             * @param x the amount of pixels to scroll by horizontally
             * @param y the amount of pixels to scroll by vertically
             */
            public void scrollBy(int x, int y) {
                scrollTo(mScrollX + x, mScrollY + y);
            }
 
我们要明白View内部两个属性mScrollX和mScrollY的改变规则，这两个属性可以通过getScrollX 和 getScrollY方法分别得到：在滑动过程中，mScrollX的值
总是等于View左边缘和View内容左边缘在水平方向的距离，而mScrollY的值总是等于View上边缘和View内容上边缘在竖直方向的距离。scrollTo和scrollBy只能
改变View内容的位置而不能改变View在布局中的位置。并且当View左边缘在View内容左边缘的右边时，mScrollX为正值，反之为负值。当View上边缘在View内容上
边缘的下边时，mScrollY为正值，反之为负值。换句话说，如果从左向右滑，mScrollX为负值，反之为正值，如果从上向下滑，mScrollY为负值，反之为正值。



2.使用动画

动画主要是操作View的translationX和translaY属性。
View的动画是对View的影响做操作，它并不能真正改变View的位置参数，包括宽高，并且希望动画后的状态得以保留还必须将fillAfter属性设置为true。属性动画
可以解决这个问题。


3.改变布局参数




















