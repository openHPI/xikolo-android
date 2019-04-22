package de.xikolo.viewmodels.announcement

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.main.AnnouncementListViewModel

class AnnouncementViewModel(val announcementId: String) : BaseViewModel() {

    private val announcementsDao = AnnouncementDao(realm)
    private val announcementListViewModel = AnnouncementListViewModel()

    val announcement: LiveData<Announcement> by lazy {
        announcementsDao.find(announcementId)
    }

    override fun onFirstCreate() {
        announcementListViewModel.onFirstCreate()
    }

    override fun onRefresh() {
        announcementListViewModel.onRefresh()
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

    override val networkState: NetworkStateLiveData
        get() = announcementListViewModel.networkState

}
