package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class ListChannelsWithCoursesJob(callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = ListChannelsWithCoursesJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().listChannelsWithCourses().awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Channels received")

            val ids = Sync.Data.with(Channel::class.java, *response.body()!!).run()
            Sync.Included.with(Course::class.java, *response.body()!!)
                .addFilter("channelId", ids)
                .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching channels list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}