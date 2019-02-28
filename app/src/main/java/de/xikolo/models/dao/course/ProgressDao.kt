package de.xikolo.models.dao.course

import androidx.lifecycle.LiveData
import de.xikolo.models.SectionProgress
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.kotlin.where

class ProgressDao(realm: Realm) : BaseDao(realm) {

    fun sectionProgressesForCourse(courseId: String): LiveData<List<SectionProgress>> =
        realm
            .where<SectionProgress>()
            .equalTo("courseProgressId", courseId)
            .sort("position")
            .findAllAsync()
            .asLiveData()

}
