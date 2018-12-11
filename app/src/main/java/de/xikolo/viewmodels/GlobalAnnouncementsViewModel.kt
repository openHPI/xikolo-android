package de.xikolo.viewmodels

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementsDao
import de.xikolo.network.jobs.ListGlobalAnnouncementsJob

open class GlobalAnnouncementsViewModel : AnnouncementsViewModel() {

    private val announcementsDao = AnnouncementsDao(realm)

    val globalAnnouncements: LiveData<List<Announcement>> by lazy {
        announcementsDao.getGlobalAnnouncements()
    }

    override fun onFirstCreate() {
        requestGlobalAnnouncementList(false)
    }

    override fun onRefresh() {
        requestGlobalAnnouncementList(true)
    }

    fun requestGlobalAnnouncementList(userRequest: Boolean) {
        ListGlobalAnnouncementsJob(userRequest, networkState).run()
    }

}
