package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.AnnouncementDao
import de.xikolo.network.jobs.ListAnnouncementsJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class AnnouncementListViewModelDelegate(realm: Realm, private val courseId: String? = null) {

    private val announcementsDao = AnnouncementDao(realm)

    val announcements: LiveData<List<Announcement>> by lazy {
        announcementsDao.all()
    }

    fun requestAnnouncementList(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListAnnouncementsJob(courseId, userRequest, networkState).run()
    }

}
