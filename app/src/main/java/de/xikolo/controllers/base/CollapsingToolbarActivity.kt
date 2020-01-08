package de.xikolo.controllers.base

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import de.xikolo.R
import de.xikolo.R.id.appbar
import de.xikolo.R.id.collapsing_toolbar

abstract class CollapsingToolbarActivity : BaseActivity() {

    companion object {
        val TAG: String = CollapsingToolbarActivity::class.java.simpleName
    }

    @BindView(R.id.toolbar_image)
    lateinit var imageView: ImageView

    @BindView(appbar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(collapsing_toolbar)
    protected lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.scrim_top)
    lateinit var scrimTop: View

    @BindView(R.id.scrim_bottom)
    lateinit var scrimBottom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_blank_collapsing)
        setupActionBar(true)
        enableOfflineModeToolbar(false)
    }

    protected fun lockCollapsingToolbar(title: String) {
        appBarLayout.setExpanded(false, false)
        val lp = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        lp.height = actionBarHeight + statusBarHeight

        collapsingToolbar.isTitleEnabled = false

        toolbar?.title = title

        scrimTop.visibility = View.INVISIBLE

        scrimBottom.visibility = View.INVISIBLE
    }

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = application.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    private val actionBarHeight: Int
        get() {
            val styledAttributes = theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize))
            val result = styledAttributes.getDimension(0, 0f).toInt()
            styledAttributes.recycle()
            return result
        }

}
