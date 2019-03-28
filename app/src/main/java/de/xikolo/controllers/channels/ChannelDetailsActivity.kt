package de.xikolo.controllers.channels

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.app.NavUtils
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.helper.CollapsingToolbarHelper
import de.xikolo.models.dao.ChannelDao
import de.xikolo.utils.ShareUtil

class ChannelDetailsActivity : BaseActivity() {

    companion object {
        val TAG: String = ChannelDetailsActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var channelId: String

    //-1 do not scroll to Course
    @AutoBundleField(required = false)
    var scrollToCoursePosition = -1

    @BindView(R.id.toolbar_image)
    lateinit var imageView: ImageView

    @BindView(R.id.appbar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.scrim_top)
    lateinit var scrimTop: View

    @BindView(R.id.scrim_bottom)
    lateinit var scrimBottom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_collapsing)
        setupActionBar(true)
        enableOfflineModeToolbar(false)

        ChannelDao.Unmanaged.find(channelId)?.let { channel ->
            title = channel.title

            val color = channel.colorOrDefault
            collapsingToolbar.setContentScrimColor(color)
            collapsingToolbar.setBackgroundColor(color)
            collapsingToolbar.setStatusBarScrimColor(color)

            val tag = "content"

            if (channel.imageUrl != null) {
                GlideApp.with(this).load(channel.imageUrl).into(imageView)
            } else {
                CollapsingToolbarHelper.lockCollapsingToolbar(
                    channel.title,
                    appBarLayout,
                    collapsingToolbar,
                    toolbar,
                    scrimTop,
                    scrimBottom
                )
            }

            val fragmentManager = supportFragmentManager
            if (fragmentManager.findFragmentByTag(tag) == null) {
                val fragment = ChannelDetailsFragmentAutoBundle.builder(channelId)
                    .scrollToCoursePosition(scrollToCoursePosition).build()
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.content, fragment, tag)
                transaction.commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.action_share -> {
                ShareUtil.shareCourseLink(this, channelId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
