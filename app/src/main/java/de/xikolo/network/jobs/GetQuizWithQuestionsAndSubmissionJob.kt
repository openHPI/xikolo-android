package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.QuizQuestion
import de.xikolo.models.QuizSubmission
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import ru.gildor.coroutines.retrofit.awaitResponse

class GetQuizWithQuestionsAndSubmissionJob(
    private val quizId: String,
    networkState: NetworkStateLiveData,
    userRequest: Boolean
) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = GetQuizWithQuestionsAndSubmissionJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.getQuizWithQuestionsAndSubmission(quizId).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Quiz received")

            Sync.Included.with<QuizQuestion>(response.body()!!)
                .saveOnly()
                .run()

            Sync.Included.with<QuizSubmission>(response.body()!!)
                .saveOnly()
                .run()

            Sync.Data.with(response.body()!!)
                .saveOnly()
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.e(TAG, "Error while fetching quiz")
            error()
        }
    }
}
