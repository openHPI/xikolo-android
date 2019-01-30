package de.xikolo.viewmodels

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListCourseAnnouncementsJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementsViewModel(val courseId: String) : BaseViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        announcementsDao.getAnnouncementsForCourse(courseId)
    }

    override fun onFirstCreate() {
        requestCourseAnnouncementList(courseId, false)
    }

    override fun onRefresh() {
        requestCourseAnnouncementList(courseId, true)
    }

    private fun requestCourseAnnouncementList(courseId: String, userRequest: Boolean) {
        ListCourseAnnouncementsJob(courseId, userRequest, networkState).run()
    }

}
