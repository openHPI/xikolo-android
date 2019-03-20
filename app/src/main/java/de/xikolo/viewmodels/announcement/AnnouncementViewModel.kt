package de.xikolo.viewmodels.announcement

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.main.AnnouncementListViewModel

class AnnouncementViewModel(val courseId: String? = null) : BaseViewModel() {

    private val announcementListViewModel = AnnouncementListViewModel()

    val announcements: LiveData<List<Announcement>> = announcementListViewModel.announcements

    override fun onFirstCreate() {
        announcementListViewModel.requestAnnouncementList(false)
    }

    override fun onRefresh() {
        announcementListViewModel.requestAnnouncementList(true)
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }
}
