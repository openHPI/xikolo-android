package de.xikolo.controllers.announcement

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.R.id.appbar
import de.xikolo.R.id.collapsing_toolbar
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.helper.CollapsingToolbarHelper
import de.xikolo.models.dao.AnnouncementDao

class AnnouncementActivity : BaseActivity() {

    companion object {
        val TAG: String = AnnouncementActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var announcementId: String

    @AutoBundleField
    var global: Boolean = false

    @BindView(R.id.toolbar_image)
    lateinit var imageView: ImageView

    @BindView(appbar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(collapsing_toolbar)
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

        AnnouncementDao.Unmanaged.find(announcementId)?.let { announcement ->
            title = announcement.title

            if (announcement.imageUrl != null) {
                GlideApp.with(this).load(announcement.imageUrl).into(imageView)
            } else if (announcement.courseId != null) {
                val course = announcement.course
                if (course?.imageUrl != null) {
                    GlideApp.with(this).load(course.imageUrl).into(imageView)
                } else {
                    CollapsingToolbarHelper.lockCollapsingToolbar(
                        announcement.title,
                        appBarLayout,
                        collapsingToolbar,
                        toolbar,
                        scrimTop,
                        scrimBottom
                    )
                }
            } else {
                CollapsingToolbarHelper.lockCollapsingToolbar(
                    announcement.title,
                    appBarLayout,
                    collapsingToolbar,
                    toolbar,
                    scrimTop,
                    scrimBottom
                )
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

}
