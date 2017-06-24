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

通过改变LayoutParams布局参数实现，如何重新设置一个View的LayoutParams呢，方法如下：

                ViewGroup.MarginLayoutParams params = mButton1.getLayoutParams();
                params.width += 100;
                params.leftMargin += 100;
                mButton1.requestLayout();
                //或者mButton1.setLayoutParams(params);



4.各种滑动方式的对比：

（1）scrollTo/scrollBy：它只能滑动View的内容，并不能滑动View本身。
（2）动画：它只能滑动View的内容，并不能滑动View本身。属性动画除外，没有明显缺点。
（3）改变布局：使用稍微麻烦，没有明显缺点。

下面是实现一个跟手滑动效果的例子：

         public boolean onTouchEvent(MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                Log.d("info","X:"+x+",Y:"+y);
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = x-mLastX;
                        int moveY = y-mLastY;
                        Log.d("info","mLastX:"+mLastX+",mLastY:"+mLastY);
                        Log.d("info","moveX:"+moveX+",moveY:"+moveY);
                        int translationX = (int) (this.getTranslationX()+moveX);
                        int translationY = (int) (this.getTranslationY()+moveY);
                        Log.d("info","translationX:"+this.getTranslationX()+",translationY:"+this.getTranslationX());
                        this.setTranslationX(translationX);
                        this.setTranslationY(translationY);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                mLastX = x;
                mLastY = y;
                return true;
            }

移动方法时采用动画兼容库nineoldandroids中的ViewHelper类所提供的setTranslationX和setTranslationY方法。



三.弹性滑动

1.使用Scroller

工作机制：不断重绘

Scroller的使用步骤1,2,3代码：

        mScroller = new Scroller(context);

        public void smoothScrollTo(int destX,int destY){
                int scrollX = getScrollX();//View左边缘和View内容左边缘的在水平方向的距离
                int delta = destX - scrollX;//滑动的距离
                //1000ms内滑向destX
                mScroller.startScroll(scrollX,0,delta,0,1000);
                invalidate();//调用draw()方法，draw方法调用computeScroll()
        }

        public void computeScroll() {
                // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
                if(mScroller.computeScrollOffset()){
                    scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
                    postInvalidate();//调用draw方法，在调用computeScroll()
                }
         }

invalidate()方法会导致View重绘，在View的draw方法中又会去调用computeScroll()方法，computeScroll()在View中是一个空实现，因此需要我们自己
去实现，上面代码已经实现了computeScroll()方法，正因为这个View才能实现弹性滑动：当View重绘后，会在draw方法中调用computeScroll()，computeScroll又会去向Scroller获取当前的scrollX和scrollY，然后通过scrollTo方法实现滑动；接着又调用postInvalidate方法来进行第二次重绘，这次
重绘和第一次重绘一样会导致computeScroll()被调用，然后继续想Scroller获取当前scrollX和scrollY，并通过scrollTo方法滑到新的位置，如此反复，直到
整个滑动过程结束。

我们看一下Scroller的computeScrollOffset()方法的实现：

          public boolean computeScrollOffset() {
                if (mFinished) {
                    return false;
                }
                int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);

                if (timePassed < mDuration) {
                    switch (mMode) {
                    case SCROLL_MODE:
                        final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                        mCurrX = mStartX + Math.round(x * mDeltaX);
                        mCurrY = mStartY + Math.round(x * mDeltaY);
                        break;
                    case FLING_MODE:
                        final float t = (float) timePassed / mDuration;
                        final int index = (int) (NB_SAMPLES * t);
                        float distanceCoef = 1.f;
                        float velocityCoef = 0.f;
                        if (index < NB_SAMPLES) {
                            final float t_inf = (float) index / NB_SAMPLES;
                            final float t_sup = (float) (index + 1) / NB_SAMPLES;
                            final float d_inf = SPLINE_POSITION[index];
                            final float d_sup = SPLINE_POSITION[index + 1];
                            velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                            distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                        }

                        mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;

                        mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                        // Pin to mMinX <= mCurrX <= mMaxX
                        mCurrX = Math.min(mCurrX, mMaxX);
                        mCurrX = Math.max(mCurrX, mMinX);

                        mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                        // Pin to mMinY <= mCurrY <= mMaxY
                        mCurrY = Math.min(mCurrY, mMaxY);
                        mCurrY = Math.max(mCurrY, mMinY);

                        if (mCurrX == mFinalX && mCurrY == mFinalY) {
                            mFinished = true;
                        }

                        break;
                    }
                }
                else {
                    mCurrX = mFinalX;
                    mCurrY = mFinalY;
                    mFinished = true;
                }
                return true;
            }

这个过程类似于动画中插值器的概念，这个方法的返回值为true，表示滑动还没结束。


通过上面的分析，我们应该明白Scroller的工作原理了，它需要配合View的computeScroll方法才能完成弹性滑动的效果，他不断让View重绘，每一次重绘距滑动
起始时间会有一个时间间隔，通过这个时间间隔Scroller就可以得出View的当前的滑动位置，知道了滑动位置就可以通过scrollTo方法来完成View的滑动。就这样，
View的每一次重绘都会导致View进行小幅度滑动，而多次的小幅度滑动就组成了弹性滑动，这就是Scroller的工作机制。



2.通过动画

因为动画自带插值器和估值器，所以我们可以利用动画的特性来实现一些动画不能实现的效果，配合scrollTo实现弹性滑动的代码如下：

        final int startX = 0;
        final int deltaX = 0;
        ValueAnimation animator = ValueAnimator.ofInt(0,1).setDuration(1000);
        animator.addUpdateListener(new AnimationUpdateListener(){
                
                onAnimationUpdate(ValueAnimation animator){
                        float fraction = animator.getAnimatedFraction();//估值器，没帧移动的百分比，每10ms一帧，执行一次这个方法
                        mButton1.scrollTo(startX + (int)(deltaX * fraction),0);
                }
        });
        animator.start();

这里的滑动针对的是View的内容而非本身。




3.使用延时策略

它的核心思想是通过发送一系列延时消息从而达到一种渐进式的效果。具体来说可以使用Handler或View的postDelayed方法，也可以使用线程的sleep方法。
对于postDelayed方法来说，我们可以通过它来发送一个延时消息，然后再消息中来进行View的滑动，如果接连不断的发送这种延时消息，那么就可以实现弹性滑动
的效果
对于线程来说，通过在while循环中不断滑动View和Sleep就可以实现弹性滑动的效果。




四.View的事件分发机制








































































