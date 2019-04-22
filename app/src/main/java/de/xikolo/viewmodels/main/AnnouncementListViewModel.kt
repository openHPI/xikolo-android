package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.network.jobs.ListAnnouncementsJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementListViewModel(val courseId: String? = null) : BaseViewModel() {

    private val announcementsDao = AnnouncementDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        if (courseId != null) {
            announcementsDao.allForCourse(courseId)
        } else {
            announcementsDao.all()
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
