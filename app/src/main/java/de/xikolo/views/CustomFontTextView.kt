package de.xikolo.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import de.xikolo.R
import de.xikolo.config.Config
import java.util.*

class CustomFontTextView : AppCompatTextView {

    companion object {
        private val TAG = CustomFontTextView::class.java.simpleName
        private val cache = Hashtable<String, Typeface>()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setCustomFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setCustomFont(context, attrs)
    }

    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView)
        val customFont = a.getString(R.styleable.CustomFontTextView_customFont)
        setCustomFont(ctx, customFont)
        a.recycle()
    }

    fun setCustomFont(ctx: Context, asset: String?): Boolean {
        var tf: Typeface?
        synchronized(cache) {
            try {
                if (!cache.containsKey(asset)) {
                    tf = Typeface.createFromAsset(ctx.assets, Config.FONT_DIR + asset!!)
                    cache[asset] = tf
                }
                tf = cache[asset]
                typeface = tf
            } catch (e: Exception) {
                Log.e(TAG, "Could not get typeface: " + e.message)
                return false
            }

            return true
        }

    }

}
