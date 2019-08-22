package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class ListEnrollmentsJob(networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListEnrollmentsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listEnrollments().awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Enrollments received")

            Sync.Data.with(response.body()!!)

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching enrollment list")
            error()
        }
    }

}
