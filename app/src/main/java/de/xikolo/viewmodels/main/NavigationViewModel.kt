package de.xikolo.viewmodels.main

import de.xikolo.R
import de.xikolo.managers.UserManager
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.models.dao.UserDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.AnnouncementListDelegate

class NavigationViewModel : BaseViewModel() {

    private val announcementListDelegate = AnnouncementListDelegate(realm)

    val announcements = announcementListDelegate.announcements

    var drawerSection: Int =
        if (UserManager.isAuthorized) {
            R.id.navigation_my_courses
        } else {
            R.id.navigation_all_courses
        }

    val user
        get() = UserDao.Unmanaged.current

    val unreadAnnouncementsCount
        get() = AnnouncementDao.Unmanaged.countNotVisited()

    override fun onFirstCreate() {
        announcementListDelegate.requestAnnouncementList(networkState, false)
    }

    override fun onRefresh() {
        announcementListDelegate.requestAnnouncementList(networkState, true)
    }

}
