package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import io.realm.kotlin.where
import ru.gildor.coroutines.retrofit.awaitResponse

class ListCoursesJob(networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = ListCoursesJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = if (UserManager.isAuthorized) {
            ApiService.instance.listCoursesWithEnrollments().awaitResponse()
        } else {
            ApiService.instance.listCourses().awaitResponse()
        }

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Courses received")

            Sync.Included.with<Enrollment>(response.body()!!)
                .run()
            Sync.Data.with(response.body()!!)
                .setBeforeCommitCallback { realm, model ->
                    val course = realm.where<Course>().equalTo("id", model.id).findFirst()
                    if (course != null) model.description = course.description
                }
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching courses list")
            error()
        }
    }

}
