package de.xikolo.viewmodels.announcement

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.AnnouncementListViewModelDelegate

class AnnouncementViewModel(private val announcementId: String) : BaseViewModel() {

    private val announcementListDelegate = AnnouncementListViewModelDelegate(realm)

    private val announcementsDao = AnnouncementDao(realm)

    val announcement: LiveData<Announcement> by lazy {
        announcementsDao.find(announcementId)
    }

    override fun onFirstCreate() {
        announcementListDelegate.requestAnnouncementList(networkState, false)
    }

    override fun onRefresh() {
        announcementListDelegate.requestAnnouncementList(networkState, true)
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
