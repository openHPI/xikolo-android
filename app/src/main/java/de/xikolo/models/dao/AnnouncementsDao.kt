package de.xikolo.models.dao

import android.arch.lifecycle.LiveData
import de.xikolo.models.Announcement
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class AnnouncementsDao(realm: Realm) : BaseDao(realm) {

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
