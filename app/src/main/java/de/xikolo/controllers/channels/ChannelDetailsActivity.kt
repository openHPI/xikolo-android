package de.xikolo.controllers.channels

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.CollapsingToolbarActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle
import de.xikolo.models.dao.ChannelDao
import de.xikolo.utils.extensions.shareCourseLink

class ChannelDetailsActivity : CollapsingToolbarActivity() {

    companion object {
        val TAG: String = ChannelDetailsActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var channelId: String

    //-1 do not scroll to Course
    @AutoBundleField(required = false)
    var scrollToCoursePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ChannelDao.Unmanaged.find(channelId)?.let { channel ->
            title = channel.title

            val color = channel.colorOrDefault
            collapsingToolbar.setContentScrimColor(color)
            collapsingToolbar.setBackgroundColor(color)
            collapsingToolbar.setStatusBarScrimColor(color)

            val tag = "content"

            if (channel.stageStream != null) {
                lockCollapsingToolbar(channel.title)
            } else if (channel.imageUrl != null) {
                GlideApp.with(this).load(channel.imageUrl).into(imageView)
            } else {
                lockCollapsingToolbar(channel.title)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.helpdesk, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            android.R.id.home    -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.action_share    -> {
                shareCourseLink(channelId)
                return true
            }
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
