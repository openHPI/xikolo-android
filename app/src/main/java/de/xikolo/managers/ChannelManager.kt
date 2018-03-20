package de.xikolo.managers

import de.xikolo.jobs.GetChannelJob
import de.xikolo.jobs.ListChannelsJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Channel
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults

class ChannelManager {

    companion object {
        val TAG: String = ChannelManager::class.java.simpleName
    }

    fun listChannels(realm: Realm, listener: RealmChangeListener<RealmResults<Channel>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val channelListPromise = realm
                .where(Channel::class.java)
                .findAllAsync()

        channelListPromise.addChangeListener(listener)

        return channelListPromise
    }

    fun getChannel(id: String, realm: Realm, listener: RealmChangeListener<Channel>?): Channel {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val channelPromise = realm
                .where(Channel::class.java)
                .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
                .endGroup()
                .findFirstAsync()

        channelPromise.addChangeListener(listener)

        return channelPromise
    }

    //returns true if there are two or more Channels
    fun hasChannels(realm: Realm) : Boolean {
        val channelList = realm
                .where(Channel::class.java)
                .findAll()

        if(channelList.count() < 2)
            return false

        return true
    }

    fun requestChannel(channelId: String, callback: RequestJobCallback) {
        GetChannelJob(channelId, callback).run()
    }

    fun requestChannelList(callback: RequestJobCallback) {
        ListChannelsJob(callback).run()
    }
}