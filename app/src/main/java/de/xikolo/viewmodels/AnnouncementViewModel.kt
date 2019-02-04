package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListAnnouncementsJob
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementViewModel(val courseId: String? = null) : BaseViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        if (courseId != null) {
            announcementsDao.getAnnouncementsForCourse(courseId)
        } else {
            announcementsDao.getGlobalAnnouncements()
        }
    }

    override fun onFirstCreate() {
        requestAnnouncementList(false)
    }

    override fun onRefresh() {
        requestAnnouncementList(true)
    }

    private fun requestAnnouncementList(userRequest: Boolean) {
        ListAnnouncementsJob(courseId, userRequest, networkState).run()
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
