package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.DocumentLocalization
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class ListDocumentsWithLocalizationsForCourseJob(private val courseId: String, userRequest: Boolean, networkState: NetworkStateLiveData) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListDocumentsWithLocalizationsForCourseJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listDocumentsWithLocalizationsForCourse(courseId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Documents received")

            Sync.Data.with(response.body()!!)
                .saveOnly()
                .run()
            Sync.Included.with<DocumentLocalization>(response.body()!!)
                .saveOnly()
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching documents list")
            error()
        }
    }

}
