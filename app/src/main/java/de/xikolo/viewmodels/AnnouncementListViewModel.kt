package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListGlobalAnnouncementsJob
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementListViewModel : BaseViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        announcementsDao.getGlobalAnnouncements()
    }

    override fun onFirstCreate() {
        requestGlobalAnnouncementList(false)
    }

    override fun onRefresh() {
        requestGlobalAnnouncementList(true)
    }

    private fun requestGlobalAnnouncementList(userRequest: Boolean) {
        ListGlobalAnnouncementsJob(userRequest, networkState).run()
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
