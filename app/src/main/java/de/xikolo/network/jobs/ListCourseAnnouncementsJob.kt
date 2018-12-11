package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Announcement
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.sync.Sync
import de.xikolo.viewmodels.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class ListCourseAnnouncementsJob(private val courseId: String, userRequest: Boolean, networkState: NetworkStateLiveData) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListCourseAnnouncementsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().listCourseAnnouncements(courseId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Announcements received")

            Sync.Data.with(Announcement::class.java, *response.body()!!)
                .addFilter("courseId", courseId)
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list")
            error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
