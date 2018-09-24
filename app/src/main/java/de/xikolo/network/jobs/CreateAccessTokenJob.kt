package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.ApiService
import de.xikolo.storages.UserStorage
import ru.gildor.coroutines.retrofit.awaitResponse

class CreateAccessTokenJob(private val email: String, private val password: String, callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = CreateAccessTokenJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().createToken(email, password).awaitResponse()
        val token = response.body()

        if (response.isSuccessful && token != null) {
            if (Config.DEBUG) Log.i(TAG, "AccessToken created")

            val userStorage = UserStorage()
            userStorage.accessToken = token.token
            userStorage.userId = token.userId

            callback?.success()
        } else {
            if (Config.DEBUG) Log.w(TAG, "AccessToken not created")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
