package de.xikolo.network.jobs

import android.content.Context
import android.util.Log
import androidx.work.*
import de.xikolo.config.Config
import de.xikolo.models.Announcement
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.ScheduledJob
import de.xikolo.network.sync.Local
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class UpdateAnnouncementVisitedJob(
    context: Context,
    params: WorkerParameters
) : ScheduledJob(context, params, Precondition.AUTH) {

    companion object {
        val TAG: String = UpdateAnnouncementVisitedJob::class.java.simpleName

        @JvmStatic
        fun schedule(announcementId: String) {
            if (Config.DEBUG) Log.i(TAG, "$TAG scheduled | announcement.id $announcementId")

            Local.Update.with<Announcement>(announcementId)
                    .setBeforeCommitCallback { _, model -> model.visited = true }
                    .run()

            val data = workDataOf("ann_id" to announcementId)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<UpdateAnnouncementVisitedJob>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        }
    }

    override suspend fun onRun(data: Data): Result {
        val model = Announcement.JsonModel()
        model.id = data.getString("ann_id")
        model.visited = true

        val response = ApiService.instance.updateAnnouncement(model.id, model).awaitResponse()

        return if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Announcement visit successfully updated")

            Sync.Data.with(response.body()!!)
                    .saveOnly()
                    .run()

            Result.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while updating announcement visit")
            Result.failure()
        }
    }

}
