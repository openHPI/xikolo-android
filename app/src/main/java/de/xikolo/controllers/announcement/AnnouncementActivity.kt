package de.xikolo.controllers.announcement

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.CollapsingToolbarActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle
import de.xikolo.models.dao.AnnouncementDao

class AnnouncementActivity : CollapsingToolbarActivity() {

    companion object {
        val TAG: String = AnnouncementActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var announcementId: String

    @AutoBundleField
    var global: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AnnouncementDao.Unmanaged.find(announcementId)?.let { announcement ->
            title = announcement.title

            if (announcement.imageUrl != null) {
                GlideApp.with(this).load(announcement.imageUrl).into(imageView)
            } else if (announcement.courseId != null) {
                val course = announcement.course
                if (course?.imageUrl != null) {
                    GlideApp.with(this).load(course.imageUrl).into(imageView)
                } else {
                    lockCollapsingToolbar(announcement.title)
                }
            } else {
                lockCollapsingToolbar(announcement.title)
            }

            val tag = "content"

            val fragmentManager = supportFragmentManager
            if (fragmentManager.findFragmentByTag(tag) == null) {
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(
                    R.id.content,
                    AnnouncementFragmentAutoBundle.builder(announcementId, global).build(),
                    tag
                )
                transaction.commit()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.helpdesk, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
