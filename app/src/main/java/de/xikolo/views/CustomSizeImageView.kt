package de.xikolo.views

import android.content.Context
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatImageView

class CustomSizeImageView : AppCompatImageView {

    var forcedHeight = 0
        private set
    var forcedWidth = 0
        private set

    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : super(context, attrs, defStyle)

    fun setDimensions(w: Int, h: Int) {
        this.forcedHeight = h
        this.forcedWidth = w
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(forcedWidth, forcedHeight)
    }

}
