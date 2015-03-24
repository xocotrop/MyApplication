package com.example.codal.myapplication.View;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by Codal on 18/03/15.
 */
public class CameraSurface extends SurfaceView {

    public static final double ASPECT_RATIO = (double)10.0 / 9.0;

    public CameraSurface(Context context) {
        super(context);
    }

    public CameraSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = getMeasuredWidth();
//        int height = (int) (width * ASPECT_RATIO);
        //setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
