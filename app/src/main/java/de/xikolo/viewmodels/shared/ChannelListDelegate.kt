package de.xikolo.viewmodels.shared

import de.xikolo.network.jobs.ListChannelsWithCoursesJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class ChannelListDelegate(realm: Realm) {

    fun requestChannels(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListChannelsWithCoursesJob(networkState, userRequest).run()
    }
}
