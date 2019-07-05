package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class GetAnnouncementJob(private val announcementId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = GetAnnouncementJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.getAnnouncement(announcementId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Announcement received")

            Sync.Data.with(response.body()!!)
                .saveOnly()
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course")
            error()
        }
    }

}
