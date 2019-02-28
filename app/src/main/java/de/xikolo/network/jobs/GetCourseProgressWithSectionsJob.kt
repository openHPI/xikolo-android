package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.CourseProgress
import de.xikolo.models.SectionProgress
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.sync.Sync
import de.xikolo.viewmodels.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class GetCourseProgressWithSectionsJob(private val courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

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

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching course progress")
            error()
        }
    }

}
