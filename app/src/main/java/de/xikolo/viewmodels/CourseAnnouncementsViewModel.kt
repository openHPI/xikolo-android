package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListCourseAnnouncementsJob

open class CourseAnnouncementsViewModel(val courseId: String) : AnnouncementsViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val announcementsForCourse: LiveData<List<Announcement>> by lazy {
        announcementsDao.getAnnouncementsForCourse(courseId)
    }

    override fun onFirstCreate() {
        requestCourseAnnouncementList(courseId, false)
    }

    override fun onRefresh() {
        requestCourseAnnouncementList(courseId, true)
    }

    fun requestCourseAnnouncementList(courseId: String, userRequest: Boolean) {
        ListCourseAnnouncementsJob(courseId, userRequest, networkState).run()
    }

}
