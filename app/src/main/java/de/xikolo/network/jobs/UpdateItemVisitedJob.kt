package de.xikolo.network.jobs

import android.content.Context
import android.util.Log
import androidx.work.*
import de.xikolo.config.Config
import de.xikolo.models.Item
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.ScheduledJob
import de.xikolo.network.sync.Local
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class UpdateItemVisitedJob(
    context: Context,
    params: WorkerParameters
) : ScheduledJob(context, params, Precondition.AUTH) {

    companion object {
        val TAG: String = UpdateItemVisitedJob::class.java.simpleName

        @JvmStatic
        fun schedule(itemId: String) {
            if (Config.DEBUG) Log.i(TAG, "$TAG scheduled | item.id $itemId")

            Local.Update.with<Item>(itemId)
                    .setBeforeCommitCallback { _, model -> model.visited = true }
                    .run()

            val data = workDataOf("item_id" to itemId)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<UpdateItemVisitedJob>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        }
    }

    override suspend fun onRun(data: Data): Result {
        val model = Item.JsonModel()
        model.id = data.getString("item_id")
        model.visited = true

        val response = ApiService.instance.updateItem(model.id, model).awaitResponse()

        return if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Item visit successfully updated")

            Sync.Data.with(response.body()!!)
                    .saveOnly()
                    .run()

            Result.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while updating item visit")
            Result.failure()
        }
    }

}
