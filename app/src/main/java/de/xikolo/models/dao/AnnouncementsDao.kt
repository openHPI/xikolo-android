package de.xikolo.models.dao

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class AnnouncementsDao(realm: Realm) : BaseDao(realm) {

    companion object {
        // ToDo ins model
        fun get(id: String): Announcement? {
            val realm = Realm.getDefaultInstance()
            var model = realm
                .where<Announcement>()
                .equalTo("id", id)
                .findFirst()
            if (model != null) {
                model = realm.copyFromRealm(model)
            }
            return model
        }

        fun countNotVisited(): Long {
            val realm = Realm.getDefaultInstance()
            return realm
                .where<Announcement>()
                .equalTo("visited", false)
                .count()
        }
    }

    fun getGlobalAnnouncements(): LiveData<List<Announcement>> =
        realm
            .where<Announcement>()
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    fun getAnnouncementsForCourse(courseId: String): LiveData<List<Announcement>> =
        realm
            .where<Announcement>()
            .equalTo("courseId", courseId)
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

}
