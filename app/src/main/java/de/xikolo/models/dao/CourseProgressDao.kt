package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.CourseProgress
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class CourseProgressDao(realm: Realm) : BaseDao<CourseProgress>(CourseProgress::class, realm) {

    class Unmanaged {
        companion object {

            fun find(id: String?): CourseProgress? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseProgress>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

        }
    }

}
