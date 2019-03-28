package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class GetChannelWithCoursesJob(private val channelId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = GetChannelWithCoursesJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().getChannelWithCourses(channelId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Channel received")

            Sync.Data.with(Channel::class.java, response.body())
                .saveOnly()
                .run()
            Sync.Included.with(Course::class.java, response.body())
                .saveOnly()
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching channel")
            error()
        }
    }

}
