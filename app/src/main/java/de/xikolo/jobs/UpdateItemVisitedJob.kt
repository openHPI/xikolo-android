package de.xikolo.jobs

import android.util.Log
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import de.xikolo.config.Config
import de.xikolo.jobs.base.ScheduledJob
import de.xikolo.models.Item
import de.xikolo.models.base.Local
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService

class UpdateItemVisitedJob : ScheduledJob(Precondition.AUTH) {

    companion object {
        val TAG: String = UpdateItemVisitedJob::class.java.simpleName

        @JvmStatic
        fun schedule(itemId: String): Int {
            if (Config.DEBUG) Log.i(TAG, TAG + " scheduled | item.id " + itemId)

            Local.Update.with(Item::class.java, itemId)
                    .setBeforeCommitCallback { _, model -> model.visited = true }
                    .run()

            val extras = PersistableBundleCompat()
            extras.putString("item_id", itemId)

            return JobRequest.Builder(UpdateItemVisitedJob.TAG)
                    .setExecutionWindow(1, 30_000)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setExtras(extras)
                    .build()
                    .schedule()
        }
    }

    override fun onRun(params: Params): Result {
        val model = Item.JsonModel()
        model.id = params.extras.getString("item_id", null)
        model.visited = true

        val response = ApiService.getInstance().updateItem(model.id, model).execute()

        return if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Item visit successfully updated")

            Sync.Data.with(Item::class.java, response.body())
                    .saveOnly()
                    .run()

            Result.SUCCESS
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while updating item visit")
            Result.FAILURE
        }
    }

}
