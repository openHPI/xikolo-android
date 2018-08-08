package de.xikolo.views;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class CustomSizeImageView extends AppCompatImageView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;

    public CustomSizeImageView(Context context) {
        super(context);
    }

    public CustomSizeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSizeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    public int getForcedHeight() {
        return mForceHeight;
    }

    public int getForcedWidth() {
        return mForceWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mForceWidth, mForceHeight);
    }

}