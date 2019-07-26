package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.Video
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class VideoDao(realm: Realm) : BaseDao<Video>(Video::class, realm) {

    class Unmanaged {
        companion object {

            // Refactor all accesses of this to `ItemDao.Unmanaged.findContent(id) as Video?` at some point
            @JvmStatic
            fun find(id: String?): Video? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Video>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

        }
    }

}
