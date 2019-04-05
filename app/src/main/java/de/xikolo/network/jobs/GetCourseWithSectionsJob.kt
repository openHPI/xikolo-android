package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Section
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class GetCourseWithSectionsJob(private val courseId: String, callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = GetCourseWithSectionsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.getCourseWithSections(courseId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Course received")

            Sync.Data.with(response.body()!!)
                    .saveOnly()
                    .run()
            Sync.Included.with<Section>(response.body()!!)
                    .addFilter("courseId", courseId)
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
