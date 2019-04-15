package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import de.xikolo.R

class MaxWidthFrameLayout : FrameLayout {

    private val maxWidth: Int

    constructor(context: Context) : super(context) {
        maxWidth = 0
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.MaxWidthFrameLayout)
        maxWidth = a.getDimensionPixelSize(
            R.styleable.MaxWidthFrameLayout_maxWidthLayout,
            Integer.MAX_VALUE
        )
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newWidthMeasureSpec = widthMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (maxWidth in 1..(measuredWidth - 1)) {
            val measureMode = MeasureSpec.getMode(widthMeasureSpec)
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, measureMode)
        }
        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
    }

}
