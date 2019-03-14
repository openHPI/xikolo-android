package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.models.Item
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.kotlin.where

class ItemsDao(realm: Realm) : BaseDao(realm) {

    fun accessibleItemsForCourse(courseId: String): LiveData<List<Item>> =
        realm
            .where<Item>()
            .equalTo("courseId", courseId)
            .equalTo("accessible", true)
            .findAllAsync()
            .asLiveData()

    fun listAccessibleItemsForSection(sectionId: String): LiveData<List<Item>> =
        realm
            .where<Item>()
            .equalTo("sectionId", sectionId)
            .equalTo("accessible", true)
            .sort("position")
            .findAllAsync()
            .asLiveData()

}
