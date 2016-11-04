package de.xikolo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomSizeVideoView extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;

    public CustomSizeVideoView(Context context) {
        super(context);
    }

    public CustomSizeVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSizeVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mForceHeight > 0 && mForceWidth > 0) {
            setMeasuredDimension(mForceWidth, mForceHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}