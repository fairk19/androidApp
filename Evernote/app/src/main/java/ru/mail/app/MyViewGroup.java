package ru.mail.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by anton on 22.05.14.
 */
public class MyViewGroup extends RelativeLayout{

    public float firstTouchX;
    public float firstTouchY;

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(android.view.MotionEvent ev){
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                firstTouchX = ev.getX();
                firstTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean b = shouldIntecept(ev);
                Log.i("d", String.valueOf(b));
                return b;
            default:
                break;
        }
        return false;
    }

    private boolean shouldIntecept(MotionEvent event){
        float deltaX = Math.abs(event.getX() - firstTouchX);
        float deltaY = Math.abs(event.getY() - firstTouchY);
        if( deltaX >  50 && deltaX/deltaY >3.0 ) {
            return true;
        }
        return false;
    }
}
