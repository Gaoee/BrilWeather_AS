package com.brilweather.weathershow;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/6/22.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private static final String TAG = "LEE MySwipeRe";

    private int mTouchSlop = 40;
    //分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    //分别记录上次滑动的坐标（在onInterceptTouchEvent中）
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private GiveUpTouchEventListener mGiveUpTouchEventListener;

    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN: {
                mLastX = x;
                mLastY = y;
                mLastXIntercept = x;
                mLastYIntercept = y;
                intercepted = false;
                break;
            }
            //解决swipeRefresh与listView之间的滑动冲突问题
            case MotionEvent.ACTION_MOVE:{
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if(mGiveUpTouchEventListener != null){
                    if (mGiveUpTouchEventListener.giveUpTouchEven(ev) && deltaY >= mTouchSlop){
                        intercepted = true;
                        Log.v(TAG, "intercepted = true");
                    }
                    else {
                        intercepted = false;
                        Log.v(TAG, "intercepted = false");
                    }

                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                mLastXIntercept = mLastYIntercept = 0;
                break;
            }
            default:{
                break;
            }
        }

        return super.onInterceptTouchEvent(ev) && intercepted;
    }

    public void setOnGiveUpTouchEventListener(GiveUpTouchEventListener giveUpTouchEventListener){
        mGiveUpTouchEventListener = giveUpTouchEventListener;
    }

    public interface GiveUpTouchEventListener{
        boolean giveUpTouchEven(MotionEvent ev);
    }
}
