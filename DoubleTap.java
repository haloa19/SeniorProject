package com.dteviot.epubviewer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by hyejin on 2017-04-03.
 */

//리스트뷰에서 더블탭을 실행하기 위한 클래스를 따로 정의
public class DoubleTap extends ListView {
    private GestureDetector mGesture;
    private OnDoubleClickListener onDoubleClickListener;

    interface OnDoubleClickListener{
        void onDoubleClick(View view);
    }

    public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener){
        this.onDoubleClickListener = onDoubleClickListener;
    };

    public DoubleTap(Context context) {
        super(context);
    }

    public DoubleTap(final Context context, AttributeSet attrs) {
        super(context, attrs);
        //
        mGesture = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(onDoubleClickListener!=null) {
                    onDoubleClickListener.onDoubleClick(DoubleTap.this);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if(Math.abs(e1.getX()-e2.getX())>50){
                    setTranslationX(e2.getX() - e1.getX());
                    ObjectAnimator.ofFloat(DoubleTap.this,"translationX",getTranslationX(),e2.getX()-e1.getX())
                            .setDuration(500).start();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                setTranslationX(getTranslationX()+e2.getX() - e1.getX());
                setTranslationY(getTranslationX()+e2.getY() - e1.getY());
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
    }

    public DoubleTap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){

        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
