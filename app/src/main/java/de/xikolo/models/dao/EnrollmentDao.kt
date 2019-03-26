package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.Enrollment
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class EnrollmentDao(realm: Realm) : BaseDao<Enrollment>(Enrollment::class, realm) {

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): Enrollment? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Enrollment>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            fun findForCourse(courseId: String?): Enrollment? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Enrollment>()
                        .equalTo("courseId", courseId)
                        .findFirst()
                        ?.asCopy()
                }

            fun count(): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Enrollment>().count()
                }

        }
    }

}
