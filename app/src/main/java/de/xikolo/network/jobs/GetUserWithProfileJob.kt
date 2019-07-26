package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Profile
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import de.xikolo.storages.UserStorage
import ru.gildor.coroutines.retrofit.awaitResponse

class GetUserWithProfileJob(networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = GetUserWithProfileJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.getUserWithProfile().awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "User received")

            val userStorage = UserStorage()
            userStorage.userId = response.body()!!.convertToRealmObject().id

            Sync.Data.with(response.body()!!).run()
            Sync.Included.with<Profile>(response.body()!!).run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching user")
            error()
        }
    }

}
