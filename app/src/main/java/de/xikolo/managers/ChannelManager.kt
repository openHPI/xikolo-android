package de.xikolo.managers

import de.xikolo.network.jobs.GetChannelWithCoursesJob
import de.xikolo.network.jobs.ListChannelsWithCoursesJob
import de.xikolo.network.jobs.base.RequestJobCallback
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
            .sort("position")
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

    fun requestChannelWithCourses(channelId: String, callback: RequestJobCallback) {
        GetChannelWithCoursesJob(channelId, callback).run()
    }

    fun requestChannelListWithCourses(callback: RequestJobCallback) {
        ListChannelsWithCoursesJob(callback).run()
    }

}
