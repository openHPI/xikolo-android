package de.xikolo.views

import android.graphics.Rect
import android.view.View

import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val horizontalSpacing: Int,
    private val verticalSpacing: Int,
    private val includeEdge: Boolean,
    private val recyclerViewInfo: RecyclerViewInfo
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        if (!recyclerViewInfo.isHeader(position)) {
            val spanCount = recyclerViewInfo.spanCount

            var lastHeader = -1

            for (i in position - 1 downTo 0) {
                if (recyclerViewInfo.isHeader(i)) {
                    lastHeader = i
                    break
                }
            }

            val relativePosition = if (lastHeader >= 0) {
                position - lastHeader - 1
            } else {
                position
            }

            var column = 0
            if (spanCount > 0) {
                column = relativePosition % spanCount
            }

            if (includeEdge || column != 0) {
                outRect.left = horizontalSpacing / 2
            }
            if (includeEdge || column != spanCount - 1) {
                outRect.right = horizontalSpacing / 2
            }
            if (includeEdge || relativePosition >= spanCount) {
                outRect.top = verticalSpacing / 2
            }
            if (includeEdge || position < recyclerViewInfo.itemCount - spanCount) {
                outRect.bottom = verticalSpacing / 2
            }
        }
    }

    interface RecyclerViewInfo {
        val spanCount: Int
        val itemCount: Int
        fun isHeader(position: Int): Boolean
    }

}
