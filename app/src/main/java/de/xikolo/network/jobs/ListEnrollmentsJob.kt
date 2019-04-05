package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class ListEnrollmentsJob(callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = ListEnrollmentsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listEnrollments().awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Enrollments received")

            Sync.Data.with(response.body()!!)

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching enrollment list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
