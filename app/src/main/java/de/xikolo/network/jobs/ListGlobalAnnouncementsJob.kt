package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Announcement
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.sync.Sync
import de.xikolo.viewmodels.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class ListGlobalAnnouncementsJob(userRequest: Boolean, networkState: NetworkStateLiveData) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = ListGlobalAnnouncementsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        // includes course announcements if auth is provided
        val response = ApiService.getInstance().listGlobalAnnouncements().awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Announcements received")

            Sync.Data.with(Announcement::class.java, *response.body()!!).run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list")
            error()
        }
    }

}
