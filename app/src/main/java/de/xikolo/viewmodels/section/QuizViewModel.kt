package de.xikolo.viewmodels.section

import androidx.lifecycle.LiveData
import de.xikolo.models.Quiz
import de.xikolo.models.QuizSubmission
import de.xikolo.models.dao.QuizQuestionDao
import de.xikolo.models.dao.QuizSubmissionDao
import de.xikolo.network.jobs.CreateQuizSubmissionJob
import de.xikolo.network.jobs.GetQuizWithQuestionsAndSubmissionJob
import de.xikolo.network.jobs.UpdateQuizSubmissionJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.viewmodels.section.base.ItemViewModel

class QuizViewModel(itemId: String, private val quizId: String) : ItemViewModel(itemId) {

    private val quizQuestionDao = QuizQuestionDao(realm)
    private val quizSubmissionDao = QuizSubmissionDao(realm)

    var quiz: Quiz? = null

    val questions
        get() = quizQuestionDao.allForQuiz(quizId)

    var newestSubmission: LiveData<QuizSubmission>? = null
        get() = quizSubmissionDao.find(quiz?.newestSubmissionId)
        private set

    override fun onFirstCreate() {
        super.onFirstCreate()
        requestQuiz(false)
    }

    override fun onRefresh() {
        super.onRefresh()
        requestQuiz(true)
    }

    private fun requestQuiz(userRequest: Boolean) {
        GetQuizWithQuestionsAndSubmissionJob(quizId, networkState, userRequest).run()
    }

    fun createQuizSubmission(networkState: NetworkStateLiveData) {
        CreateQuizSubmissionJob(quizId, networkState, true).run()
    }

    fun updateQuizSubmission(submission: QuizSubmission, networkState: NetworkStateLiveData) {
        UpdateQuizSubmissionJob(submission, networkState, false).run()
    }
}
