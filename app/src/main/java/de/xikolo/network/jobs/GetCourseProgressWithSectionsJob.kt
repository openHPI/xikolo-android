package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.models.CourseProgress
import de.xikolo.models.SectionProgress
import de.xikolo.network.sync.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class GetCourseProgressWithSectionsJob(private val courseId: String, callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = GetCourseProgressWithSectionsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().getCourseProgressWithSections(courseId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Course progress received")

            Sync.Data.with(CourseProgress::class.java, response.body())
                    .saveOnly()
                    .run()
            Sync.Included.with(SectionProgress::class.java, response.body())
                    .addFilter("courseProgressId", courseId)
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course progress")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
