package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.Profile
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class ProfileDao(realm: Realm) : BaseDao<Profile>(Profile::class, realm) {

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): Profile? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Profile>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

        }
    }

}
