package de.xikolo.viewmodels.announcement

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.network.jobs.GetAnnouncementJob
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel

class AnnouncementViewModel(private val announcementId: String) : BaseViewModel() {

    private val announcementsDao = AnnouncementDao(realm)

    val announcement: LiveData<Announcement> by lazy {
        announcementsDao.find(announcementId)
    }

    override fun onFirstCreate() {
        requestAnnouncement(false)
    }

    override fun onRefresh() {
        requestAnnouncement(true)
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

    private fun requestAnnouncement(userRequest: Boolean) {
        GetAnnouncementJob(announcementId, networkState, userRequest).run()
    }

}
