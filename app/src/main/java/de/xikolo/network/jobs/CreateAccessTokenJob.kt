package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.storages.UserStorage
import ru.gildor.coroutines.retrofit.awaitResponse

class CreateAccessTokenJob(private val email: String, private val password: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = CreateAccessTokenJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.createToken(email, password).awaitResponse()
        val token = response.body()

        if (response.isSuccessful && token != null) {
            if (Config.DEBUG) Log.i(TAG, "AccessToken created")

            val userStorage = UserStorage()
            userStorage.accessToken = token.token
            userStorage.userId = token.userId

            success()
        } else {
            if (Config.DEBUG) Log.w(TAG, "AccessToken not created")
            error()
        }
    }

}
