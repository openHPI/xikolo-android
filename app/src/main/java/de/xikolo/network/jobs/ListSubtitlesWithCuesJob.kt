package de.xikolo.network.jobs


import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.SubtitleCue
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class ListSubtitlesWithCuesJob(callback: RequestJobCallback, private val videoId: String) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = ListSubtitlesWithCuesJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.listSubtitlesWithCuesForVideo(videoId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Subtitles received")

            val ids = Sync.Data.with(response.body()!!)
                    .addFilter("videoId", videoId)
                    .run()
            Sync.Included.with<SubtitleCue>(response.body()!!)
                    .addFilter("subtitleId", ids)
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching subtitle list")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
