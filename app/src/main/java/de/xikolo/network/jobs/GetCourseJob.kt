package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.sync.Sync
import de.xikolo.network.jobs.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class GetCourseJob(private val courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

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

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course")
            error()
        }
    }

}
