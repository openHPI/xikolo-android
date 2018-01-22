package de.xikolo.jobs

import android.util.Log
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import de.xikolo.config.Config
import de.xikolo.jobs.base.ScheduledJob
import de.xikolo.models.Announcement
import de.xikolo.models.base.Local
import de.xikolo.models.base.Sync
import de.xikolo.network.ApiService

class UpdateAnnouncementVisitedJob : ScheduledJob(Precondition.AUTH) {

    companion object {
        val TAG: String = UpdateAnnouncementVisitedJob::class.java.simpleName

        @JvmStatic
        fun schedule(announcementId: String): Int {
            if (Config.DEBUG) Log.i(TAG, TAG + " scheduled | announcement.id " + announcementId)

            Local.Update.with(Announcement::class.java, announcementId)
                    .setBeforeCommitCallback { _, model -> model.visited = true }
                    .run()

            val extras = PersistableBundleCompat()
            extras.putString("ann_id", announcementId)

            return JobRequest.Builder(UpdateAnnouncementVisitedJob.TAG)
                    .setExecutionWindow(1, 30_000)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setExtras(extras)
                    .build()
                    .schedule()
        }
    }

    override fun onRun(params: Params): Result {
        val model = Announcement.JsonModel()
        model.id = params.extras.getString("ann_id", null)
        model.visited = true

        val response = ApiService.getInstance().updateAnnouncement(model.id, model).execute()

        return if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Announcement visit successfully updated")

            Sync.Data.with(Announcement::class.java, response.body())
                    .saveOnly()
                    .run()

            Result.SUCCESS
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while updating announcement visit")
            Result.FAILURE
        }
    }

}
