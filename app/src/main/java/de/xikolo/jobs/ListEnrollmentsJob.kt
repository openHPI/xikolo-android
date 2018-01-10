package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.jobs.base.RequestJob
import de.xikolo.models.Enrollment
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class ListEnrollmentsJob(callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = ListEnrollmentsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().listEnrollments().awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Enrollments received")

            Sync.Data.with(Enrollment::class.java, *response.body()!!)

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching enrollment list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
