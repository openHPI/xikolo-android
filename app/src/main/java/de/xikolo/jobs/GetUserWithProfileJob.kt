package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Profile
import de.xikolo.models.User
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import de.xikolo.storages.UserStorage
import ru.gildor.coroutines.retrofit.awaitResponse

class GetUserWithProfileJob(callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = GetUserWithProfileJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().getUserWithProfile().awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "User received")

            val userStorage = UserStorage()
            userStorage.userId = response.body()?.convertToRealmObject()?.id

            Sync.Data.with(User::class.java, response.body()).run()
            Sync.Included.with(Profile::class.java, response.body()).run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
