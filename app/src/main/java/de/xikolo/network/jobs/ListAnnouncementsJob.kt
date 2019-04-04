package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Announcement
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.sync.Sync
import de.xikolo.network.jobs.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class ListAnnouncementsJob(private val courseId: String?, userRequest: Boolean, networkState: NetworkStateLiveData) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListAnnouncementsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = if (courseId != null) {
            ApiService.instance.listCourseAnnouncements(courseId).awaitResponse()
        } else {
            ApiService.instance.listGlobalAnnouncements().awaitResponse()
        }

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Announcements received")

            val sync = Sync.Data.with(Announcement::class.java, *response.body()!!)
            if (courseId != null) {
                sync.addFilter("courseId", courseId)
            }
            sync.run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list")
            error()
        }
    }

}
