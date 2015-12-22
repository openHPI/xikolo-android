package de.xikolo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import de.xikolo.R;

public class MaxWidthFrameLayout extends FrameLayout {

    private final int mMaxWidth;

    public MaxWidthFrameLayout(Context context) {
        super(context);
        mMaxWidth = 0;
    }

    public MaxWidthFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaxWidthFrameLayout);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.MaxWidthFrameLayout_maxWidth, Integer.MAX_VALUE);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth > 0 && mMaxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
