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
import io.realm.kotlin.where
import ru.gildor.coroutines.retrofit.awaitResponse

class ListCoursesJob(networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

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
                    val course = realm.where<Course>().equalTo("id", model.id).findFirst()
                    if (course != null) model.description = course.description
                }
                .run()
            Sync.Included.with(Enrollment::class.java, *response.body()!!).run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching courses list")
            error()
        }
    }

}
