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
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.Announcement
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.viewmodels.announcement.AnnouncementViewModel
import de.xikolo.views.DateTextView
import java.text.DateFormat
import java.util.Locale

class AnnouncementFragment : ViewModelFragment<AnnouncementViewModel>() {

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
    internal lateinit var date: DateTextView

    @BindView(R.id.course_button)
    internal lateinit var courseButton: Button

    override val layoutResource = R.layout.fragment_announcement

    override fun createViewModel(): AnnouncementViewModel {
        return AnnouncementViewModel(announcementId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.announcement
            .observe(viewLifecycleOwner) {
                showAnnouncement(it)
            }
    }

    private fun showAnnouncement(announcement: Announcement) {
        if (global) {
            val course = announcement.course
            if (course?.accessible == true && course.isEnrolled) {
                courseButton.visibility = View.VISIBLE
                courseButton.setOnClickListener {
                    val intent = CourseActivityAutoBundle.builder()
                        .courseId(announcement.courseId)
                        .build(requireActivity())
                    startActivity(intent)
                }
            }
        }

        if (!announcement.visited && UserManager.isAuthorized) {
            viewModel.updateAnnouncementVisited(announcementId)
        }

        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())

        announcement.publishedAt?.let {
            date.text = dateFormat.format(it)
            date.setDate(it)
        }

        text.setMarkdownText(announcement.text)

        showContent()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
            android.R.id.home -> {
                activity?.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
