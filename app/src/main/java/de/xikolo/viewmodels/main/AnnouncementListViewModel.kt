package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListAnnouncementsJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementListViewModel(val courseId: String? = null) : BaseViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        if (courseId != null) {
            announcementsDao.announcementsForCourse(courseId)
        } else {
            announcementsDao.globalAnnouncements()
        }
    }

    override fun onFirstCreate() {
        requestAnnouncementList(false)
    }

    override fun onRefresh() {
        requestAnnouncementList(true)
    }

    fun requestAnnouncementList(userRequest: Boolean) {
        ListAnnouncementsJob(courseId, userRequest, networkState).run()
    }

}
