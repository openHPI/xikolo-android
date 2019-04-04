package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Item
import de.xikolo.models.Section
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.sync.Sync
import de.xikolo.network.jobs.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class ListSectionsWithItemsJob(private val courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListSectionsWithItemsJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listSectionsWithItemsForCourse(courseId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Sections received")

            Sync.Data.with(Section::class.java, *response.body()!!)
                .addFilter("courseId", courseId)
                .run()
            Sync.Included.with(Item::class.java, *response.body()!!)
                .addFilter("courseId", courseId)
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching section list")
            error()
        }
    }

}
