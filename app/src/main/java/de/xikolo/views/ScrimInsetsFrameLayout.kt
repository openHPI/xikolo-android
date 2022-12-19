package de.xikolo.views

/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import de.xikolo.R

/**
 * A layout that draws something in the insets passed to [.fitSystemWindows], i.e. the area above UI chrome
 * (status and navigation bars, overlay action bars).
 */
class ScrimInsetsFrameLayout : FrameLayout {

    private var insetForeground: Drawable? = null

    private var insets: Rect? = null
    private val tempRect = Rect()
    private var onInsetsCallback: OnInsetsCallback? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ScrimInsetsFrameLayout, defStyle, 0)
        insetForeground = a.getDrawable(R.styleable.ScrimInsetsFrameLayout_insetFg)
        a.recycle()

        setWillNotDraw(true)
    }

    override fun fitSystemWindows(insets: Rect): Boolean {
        this.insets = Rect(insets)
        setWillNotDraw(insetForeground == null)
        ViewCompat.postInvalidateOnAnimation(this)
        onInsetsCallback?.onInsetsChanged(insets)
        return true // consume insets
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val width = width
        val height = height
        if (insets != null && insetForeground != null) {
            val sc = canvas.save()
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())

            // Top
            tempRect.set(0, 0, width, insets!!.top)
            insetForeground!!.bounds = tempRect
            insetForeground!!.draw(canvas)

            // Bottom
            tempRect.set(0, height - insets!!.bottom, width, height)
            insetForeground!!.bounds = tempRect
            insetForeground!!.draw(canvas)

            // Left
            tempRect.set(0, insets!!.top, insets!!.left, height - insets!!.bottom)
            insetForeground!!.bounds = tempRect
            insetForeground!!.draw(canvas)

            // Right
            tempRect.set(width - insets!!.right, insets!!.top, width, height - insets!!.bottom)
            insetForeground!!.bounds = tempRect
            insetForeground!!.draw(canvas)

            canvas.restoreToCount(sc)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        insetForeground?.callback = this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        insetForeground?.callback = null
    }

    /**
     * Allows the calling container to specify a callback for custom processing when insets change (i.e. when
     * [.fitSystemWindows] is called. This is useful for setting padding on UI elements based on
     * UI chrome insets (e.g. a Google Map or a ListView). When using with ListView or GridView, remember to set
     * clipToPadding to false.
     */
    fun setOnInsetsCallback(onInsetsCallback: OnInsetsCallback) {
        this.onInsetsCallback = onInsetsCallback
    }

    interface OnInsetsCallback {
        fun onInsetsChanged(insets: Rect)
    }

}
