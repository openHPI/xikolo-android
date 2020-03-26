package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.extensions.asCopy
import de.xikolo.extensions.asLiveData
import de.xikolo.models.Channel
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class ChannelDao(realm: Realm) : BaseDao<Channel>(Channel::class, realm) {

    init {
        defaultSort = "position" to Sort.ASCENDING
    }

    override fun find(id: String?): LiveData<Channel> =
        query()
            .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
            .endGroup()
            .findFirstAsync()
            .asLiveData()

    class Unmanaged {
        companion object {

            @JvmStatic
            fun all(): List<Channel> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Channel>()
                        .findAll()
                        .asCopy()
                }

            @JvmStatic
            fun find(id: String?): Channel? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Channel>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

        }
    }

}
