package de.xikolo.viewmodels.shared

import de.xikolo.network.jobs.GetUserWithProfileJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class UserDelegate(realm: Realm) {

    fun requestUserWithProfile(networkState: NetworkStateLiveData, userRequest: Boolean) {
        GetUserWithProfileJob(networkState, userRequest).run()
    }

}
