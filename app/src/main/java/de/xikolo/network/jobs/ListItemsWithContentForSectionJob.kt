package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.ItemSyncHelper
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class ListItemsWithContentForSectionJob(private val sectionId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = ListItemsWithContentForSectionJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listItemsWithContentForSection(sectionId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Items received")

            Sync.Data.with(response.body()!!)
                .addFilter("sectionId", sectionId)
                .run()
            ItemSyncHelper.syncItemContent(response.body()!!)

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching section list")
            error()
        }
    }

}
