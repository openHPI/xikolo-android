package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

/**
 * TabLayout that retains the position of the last selected tab after removeAllTabs() has been called.
 */
class PositionRetainingTabLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    TabLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(
        context,
        null,
        com.google.android.material.R.attr.tabStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        com.google.android.material.R.attr.tabStyle
    )

    private var lastSelectedTabPosition = -1

    override fun removeAllTabs() {
        lastSelectedTabPosition = selectedTabPosition
        super.removeAllTabs()
    }

    override fun getSelectedTabPosition(): Int {
        return super.getSelectedTabPosition().takeIf { it != -1 }
            ?: lastSelectedTabPosition.coerceAtMost(super.getTabCount())
    }
}
