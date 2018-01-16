package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Announcement
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class ListGlobalAnnouncementsJob(callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = ListGlobalAnnouncementsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        // includes course announcements if auth is provided
        val response = ApiService.getInstance().listGlobalAnnouncements().awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Announcements received")

            Sync.Data.with(Announcement::class.java, *response.body()!!).run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
