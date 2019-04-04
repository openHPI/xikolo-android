package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.models.Item
import de.xikolo.network.sync.Sync
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class GetItemWithContentJob(callback: RequestJobCallback, private val itemId: String) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = GetItemWithContentJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.getItemWithContent(itemId).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Item received")

            Sync.Data.with(Item::class.java, response.body())
                    .saveOnly()
                    .run()
            syncItemContent(response.body()!!)

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching section list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
