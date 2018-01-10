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

class ListCoursesJob(callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = ListCoursesJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = if (UserManager.isAuthorized) {
            ApiService.getInstance().listCoursesWithEnrollments().awaitResponse()
        } else {
            ApiService.getInstance().listCourses().awaitResponse()
        }

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Courses received")

            Sync.Data.with(Course::class.java, *response.body()!!)
                    .setBeforeCommitCallback { realm, model ->
                        val course = realm.where(Course::class.java).equalTo("id", model.id).findFirst()
                        if (course != null) model.description = course.description
                    }
                    .run()
            Sync.Included.with(Enrollment::class.java, *response.body()!!).run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching courses list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
