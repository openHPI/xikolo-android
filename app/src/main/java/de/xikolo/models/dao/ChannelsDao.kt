package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.models.Channel
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.kotlin.where

class ChannelsDao(realm: Realm) : BaseDao(realm) {

    fun channels(): LiveData<List<Channel>> =
        realm
            .where<Channel>()
            .sort("position")
            .findAllAsync()
            .asLiveData()

    fun channel(id: String): LiveData<Channel> =
        realm
            .where<Channel>()
            .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
            .endGroup()
            .findFirstAsync()
            .asLiveData()

}
