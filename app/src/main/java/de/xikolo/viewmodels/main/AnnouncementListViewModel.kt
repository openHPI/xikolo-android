package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.AnnouncementListViewModelDelegate

class AnnouncementListViewModel(private val courseId: String? = null) : BaseViewModel() {

    private val announcementListDelegate = AnnouncementListViewModelDelegate(realm, courseId)

    private val announcementsDao = AnnouncementDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        if (courseId != null) {
            announcementsDao.allForCourse(courseId)
        } else {
            announcementsDao.all()
        }
    }

    override fun onFirstCreate() {
        announcementListDelegate.requestAnnouncementList(networkState, false)
    }

    override fun onRefresh() {
        announcementListDelegate.requestAnnouncementList(networkState, true)
    }

}
