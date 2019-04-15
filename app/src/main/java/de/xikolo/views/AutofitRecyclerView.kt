package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class AutofitRecyclerView : RecyclerView {

    private var manager: GridLayoutManager = GridLayoutManager(context, 1)

    private val itemDecorations = ArrayList<ItemDecoration>()

    private var columnWidth = -1

    var spanCount: Int = 0
        private set

    var spanSizeLookup: GridLayoutManager.SpanSizeLookup
        get() = manager.spanSizeLookup
        set(spanSizeLookup) {
            manager.spanSizeLookup = spanSizeLookup
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attrsArray = intArrayOf(android.R.attr.columnWidth)
            val array = context.obtainStyledAttributes(attrs, attrsArray)
            columnWidth = array.getDimensionPixelSize(0, -1)
            array.recycle()
        }

        layoutManager = manager
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (columnWidth > 0) {
            spanCount = Math.max(1, measuredWidth / columnWidth)
            manager.spanCount = spanCount
        }
    }

    override fun addItemDecoration(decor: ItemDecoration) {
        super.addItemDecoration(decor)
        addItemDecoration(decor, -1)
    }

    override fun addItemDecoration(decor: ItemDecoration, index: Int) {
        super.addItemDecoration(decor, index)
        if (index < 0) {
            itemDecorations.add(decor)
        } else {
            itemDecorations.add(index, decor)
        }
    }

    fun clearItemDecorations() {
        for (decor in itemDecorations) {
            super.removeItemDecoration(decor)
        }
        itemDecorations.clear()
    }

}
