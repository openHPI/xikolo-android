package de.xikolo.viewmodels

import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.viewmodels.base.BaseViewModel

abstract class AnnouncementsViewModel : BaseViewModel() {

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
