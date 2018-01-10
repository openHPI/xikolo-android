package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Announcement
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class ListCourseAnnouncementsJob(private val courseId: String, callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

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

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
