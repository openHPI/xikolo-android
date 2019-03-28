package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.Announcement
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class AnnouncementDao(realm: Realm) : BaseDao<Announcement>(Announcement::class, realm) {

    init {
        defaultSort = "publishedAt" to Sort.DESCENDING
    }

    fun allForCourse(courseId: String?) =
        all("courseId" to courseId)

    class Unmanaged {
        companion object {

            fun find(id: String?): Announcement? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Announcement>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            @JvmStatic
            fun countNotVisited(): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Announcement>()
                        .equalTo("visited", false)
                        .count()
                }

        }
    }

}
