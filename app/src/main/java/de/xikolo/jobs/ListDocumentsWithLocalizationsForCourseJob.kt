package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.NetworkJob
import de.xikolo.viewmodels.base.NetworkStateLiveData
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class ListDocumentsWithLocalizationsForCourseJob(private val courseId: String, userRequest: Boolean, networkState: NetworkStateLiveData) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListDocumentsWithLocalizationsForCourseJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().listDocumentsWithLocalizationsForCourse(courseId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Documents received")

            Sync.Data.with(Document::class.java, *response.body()!!)
                .saveOnly()
                .run()
            Sync.Included.with(DocumentLocalization::class.java, *response.body()!!)
                .saveOnly()
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching documents list")
            error()
        }
    }

}
