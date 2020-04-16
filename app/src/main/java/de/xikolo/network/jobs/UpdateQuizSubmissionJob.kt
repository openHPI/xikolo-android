package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.QuizSubmission
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import ru.gildor.coroutines.retrofit.awaitResponse

class UpdateQuizSubmissionJob(
    private val submission: QuizSubmission,
    networkState: NetworkStateLiveData,
    userRequest: Boolean
) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = UpdateQuizSubmissionJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val submissionBody = submission.convertToJsonResource()
        val response = ApiService.instance.updateQuizSubmission(submissionBody.id, submissionBody)
            .awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Submission updated")
            success()
        } else {
            error()
        }
    }
}
