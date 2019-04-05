package de.xikolo.views

import android.content.Context
import android.util.AttributeSet

class CustomSizeVideoView : ExoPlayerVideoView {

    private var forceHeight = 0
    private var forceWidth = 0

    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (forceHeight > 0 && forceWidth > 0) {
            setMeasuredDimension(forceWidth, forceHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}
