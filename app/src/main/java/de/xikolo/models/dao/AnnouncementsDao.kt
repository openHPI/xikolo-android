package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class AnnouncementsDao(realm: Realm) : BaseDao(realm) {

    fun announcement(id: String): LiveData<Announcement> =
        realm
            .where<Announcement>()
            .equalTo("id", id)
            .findFirstAsync()
            .asLiveData()

    fun markAnnouncementVisited(id: String) {
        val localRealm = Realm.getDefaultInstance()
        var model = localRealm
            .where<Announcement>()
            .equalTo("id", id)
            .findFirst()
        model?.let {
            model = localRealm.copyFromRealm(it)
        }
        localRealm.close()
        model?.visited = true
    }

    fun globalAnnouncements(): LiveData<List<Announcement>> =
        realm
            .where<Announcement>()
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    fun announcementsForCourse(courseId: String): LiveData<List<Announcement>> =
        realm
            .where<Announcement>()
            .equalTo("courseId", courseId)
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

}
