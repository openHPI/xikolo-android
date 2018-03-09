package de.xikolo.managers

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

    //returns true if there are more than two Channels
    fun hasChannels(realm: Realm) : Boolean {
        val channelList = realm
                .where(Channel::class.java)
                .findAll()

        if(channelList.size < 2)
            return false

        return true
    }

    fun requestChannelList(callback: RequestJobCallback) {
        ListChannelsJob(callback).run()
    }
}