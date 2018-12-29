package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListCourseAnnouncementsJob
import de.xikolo.network.jobs.ListGlobalAnnouncementsJob
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementsViewModel(val courseId: String? = null) : BaseViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val courseAnnouncements: LiveData<List<Announcement>> by lazy {
        if(courseId != null) {
            announcementsDao.getAnnouncementsForCourse(courseId)
        } else {
            throw IllegalArgumentException("courseId must not be null to access this field")
        }
    }

    val globalAnnouncements: LiveData<List<Announcement>> by lazy {
        announcementsDao.getGlobalAnnouncements()
    }

    override fun onFirstCreate() {
        if(courseId != null) {
            requestCourseAnnouncementList(courseId, false)
        }
        requestGlobalAnnouncementList(false)
    }

    override fun onRefresh() {
        if(courseId != null) {
            requestCourseAnnouncementList(courseId, true)
        }
        requestGlobalAnnouncementList(true)
    }

    private fun requestCourseAnnouncementList(courseId: String, userRequest: Boolean) {
        ListCourseAnnouncementsJob(courseId, userRequest, networkState).run()
    }

    private fun requestGlobalAnnouncementList(userRequest: Boolean) {
        ListGlobalAnnouncementsJob(userRequest, networkState).run()
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
