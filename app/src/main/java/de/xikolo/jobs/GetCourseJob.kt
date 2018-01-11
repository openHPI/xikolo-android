package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.jobs.base.RequestJob
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class GetCourseJob(private val courseId: String, callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = GetCourseJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = if (UserManager.isAuthorized) {
            ApiService.getInstance().getCourseWithEnrollment(courseId).awaitResponse()
        } else {
            ApiService.getInstance().getCourse(courseId).awaitResponse()
        }

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Course received")

            Sync.Data.with(Course::class.java, response.body())
                    .saveOnly()
                    .run()
            Sync.Included.with(Enrollment::class.java, response.body())
                    .addFilter("courseId", courseId)
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
