package de.xikolo.controllers.announcement

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.managers.UserManager
import de.xikolo.models.Announcement
import de.xikolo.models.Course
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.AnnouncementsViewModel
import de.xikolo.viewmodels.GlobalAnnouncementsViewModel
import java.text.DateFormat
import java.util.*

class AnnouncementFragment : NetworkStateFragment<AnnouncementsViewModel>() {

    companion object {
        val TAG: String = AnnouncementFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var announcementId: String

    @AutoBundleField
    var global: Boolean = false

    @BindView(R.id.text)
    internal lateinit var text: TextView
    @BindView(R.id.date)
    internal lateinit var date: TextView
    @BindView(R.id.course_button)
    internal lateinit var courseButton: Button

    private var announcement: Announcement? = null

    override val layoutResource = R.layout.content_announcement

    override fun createViewModel(): AnnouncementsViewModel {
        return GlobalAnnouncementsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        announcement = Announcement.get(announcementId)
        announcement?.let {
            showAnnouncement(it)
        }
    }

    private fun showAnnouncement(announcement: Announcement) {
        if (global && announcement.courseId != null) {
            val course = Course.get(announcement.courseId)
            if (course.accessible && course.isEnrolled) {
                courseButton.visibility = View.VISIBLE
                courseButton.setOnClickListener { _ ->
                    val intent = CourseActivityAutoBundle.builder().courseId(announcement.courseId).build(activity!!)
                    startActivity(intent)
                }
            }
        }

        if (!announcement.visited && UserManager.isAuthorized) {
            viewModel.updateAnnouncementVisited(announcementId)
            announcement.visited = true
        }

        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        date.text = dateFormat.format(announcement.publishedAt)

        MarkdownUtil.formatAndSet(announcement.text, text)

        showContent()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
            android.R.id.home   -> {
                activity?.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
