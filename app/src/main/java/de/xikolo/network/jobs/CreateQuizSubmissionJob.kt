package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Quiz
import de.xikolo.models.QuizSubmission
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import moe.banana.jsonapi2.HasOne
import ru.gildor.coroutines.retrofit.awaitResponse

class CreateQuizSubmissionJob(
    private val quizId: String,
    networkState: NetworkStateLiveData,
    userRequest: Boolean
) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = CreateQuizSubmissionJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val submission = QuizSubmission.JsonModel()
        submission.quiz = HasOne(Quiz.JsonModel().type, quizId)

        val response = ApiService.instance.createQuizSubmission(submission).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Submission created")
            success()
        } else {
            error()
        }
    }
}
