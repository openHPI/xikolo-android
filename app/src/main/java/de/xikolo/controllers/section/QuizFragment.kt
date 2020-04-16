package de.xikolo.controllers.section

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.models.Quiz
import de.xikolo.models.QuizQuestion
import de.xikolo.models.QuizSubmission
import de.xikolo.models.dao.ItemDao
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.viewmodels.section.QuizViewModel
import de.xikolo.views.DateTextView
import de.xikolo.views.quiz.FreeTextQuestionView
import de.xikolo.views.quiz.MultiChoiceQuestionView
import de.xikolo.views.quiz.QuestionContainerView
import de.xikolo.views.quiz.QuestionView
import de.xikolo.views.quiz.SingleChoiceQuestionView
import io.realm.RealmList
import java.text.DateFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class QuizFragment : ViewModelFragment<QuizViewModel>() {

    companion object {
        val TAG: String = QuizFragment::class.java.simpleName

        const val SNAPSHOTTING_INTERVAL = 20000L
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @AutoBundleField
    lateinit var quizId: String

    @BindView(R.id.snapshotContainer)
    lateinit var snapshotContainer: ViewGroup

    @BindView(R.id.snapshotMessage)
    lateinit var snapshotMessage: TextView

    @BindView(R.id.snapshotCheckmark)
    lateinit var snapshotCheckmark: TextView

    @BindView(R.id.snapshotProgress)
    lateinit var snapshotProgress: ProgressBar

    @BindView(R.id.message)
    lateinit var message: TextView

    @BindView(R.id.questionContainer)
    lateinit var questionContainer: ViewGroup

    @BindView(R.id.detailsQuestionCount)
    lateinit var questionCount: TextView

    @BindView(R.id.detailsInstructions)
    lateinit var instructions: TextView

    @BindView(R.id.details)
    lateinit var detailsContainer: View

    @BindView(R.id.detailsPoints)
    lateinit var maxPoints: TextView

    @BindView(R.id.detailsTimeLimit)
    lateinit var timeLimit: TextView

    @BindView(R.id.detailsAllowedAttempts)
    lateinit var allowedAttempts: TextView

    @BindView(R.id.detailsSubmission)
    lateinit var submissionDetailsContainer: View

    @BindView(R.id.detailsAchievedPoints)
    lateinit var achievedPoints: TextView

    @BindView(R.id.detailsLastSubmitted)
    lateinit var lastSubmitted: DateTextView

    override val layoutResource = R.layout.fragment_quiz

    private var questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>> = mapOf()
    private var isInSolutionMode = false

    private var snapshotTimer: Timer? = null

    override fun createViewModel(): QuizViewModel {
        return QuizViewModel(itemId, quizId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The PagerAdapter creates the Fragments multiple times, so they need to be reset here
        resetView()

        setSubmissionMode()

        viewModel.item
            .observe(viewLifecycleOwner) { item ->
                ItemDao.Unmanaged.findContent(item.id)?.let { quiz ->
                    viewModel.quiz = quiz as Quiz

                    message.text = getString(R.string.quiz_selftest_description)

                    if (quiz.instructions != "dummy") {
                        instructions.setMarkdownText(quiz.instructions)
                        instructions.visibility = View.VISIBLE
                    } else {
                        instructions.visibility = View.GONE
                    }

                    maxPoints.text =
                        getString(R.string.quiz_maximum_points, item.maxPoints.toString())

                    timeLimit.text =
                        if (quiz.timeLimit == 0) {
                            getString(R.string.quiz_time_limit_none)
                        } else {
                            getString(R.string.quiz_time_limit, quiz.timeLimit)
                        }

                    if (quiz.allowedAttempts > 0) {
                        allowedAttempts.text =
                            getString(R.string.quiz_allowed_attempts, quiz.allowedAttempts)
                        allowedAttempts.visibility = View.VISIBLE
                    } else {
                        allowedAttempts.visibility = View.GONE
                    }
                }
            }

        viewModel.questions
            .observe(viewLifecycleOwner) { questions ->
                questionCount.text = getString(R.string.quiz_question_count, questions.size)

                if (questionViewMap.isEmpty()) {
                    questionViewMap = buildQuestions(questions)
                }

                viewModel.newestSubmission?.removeObservers(viewLifecycleOwner)
                viewModel.newestSubmission
                    ?.observe(viewLifecycleOwner) { submission ->
                        lockQuestions(questionViewMap)
                        insertAnswers(questionViewMap, submission)

                        if (submission.submitted) {
                            showSolution(questionViewMap, submission)
                            setSolutionMode()

                            viewModel.item.value?.maxPoints?.let {
                                achievedPoints.text = getString(
                                    R.string.quiz_submission_points,
                                    submission.points.toString(),
                                    it.toString()
                                )
                                achievedPoints.visibility = View.VISIBLE
                            } ?: run {
                                achievedPoints.visibility = View.GONE
                            }

                            submission.submittedAt?.let {
                                lastSubmitted.setDate(it)
                                lastSubmitted.text = getString(
                                    R.string.quiz_submission_submitted_at,
                                    DateFormat.getDateTimeInstance(
                                        DateFormat.YEAR_FIELD
                                            or DateFormat.MONTH_FIELD
                                            or DateFormat.DATE_FIELD,
                                        DateFormat.SHORT,
                                        Locale.getDefault()
                                    ).format(it)
                                )
                                lastSubmitted.visibility = View.VISIBLE
                            } ?: run {
                                lastSubmitted.visibility = View.GONE
                            }
                        } else {
                            unlockQuestions(questionViewMap)
                        }

                        showContent()
                    }

                showContent()
            }
    }

    override fun onStart() {
        super.onStart()
        if (!isInSolutionMode) {
            startSnapshotting()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isInSolutionMode) {
            stopSnapshotting()
            snapshot()
        }
    }

    private fun snapshot() {
        if (viewModel.quiz?.newestSubmissionId != null) {
            updateQuizSubmission(getAnswers(questionViewMap), false)
        } else {
            createQuizSubmission(true)
        }
    }

    private fun startSnapshotting() {
        if (snapshotTimer == null) {
            snapshotTimer?.cancel()
            snapshotTimer = Timer()
            snapshotTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (!isInSolutionMode) {
                        activity?.runOnUiThread {
                            snapshot()
                        }
                    }
                }
            }, SNAPSHOTTING_INTERVAL, SNAPSHOTTING_INTERVAL)
        }
    }

    private fun stopSnapshotting() {
        snapshotTimer?.cancel()
        snapshotTimer = null
    }

    private fun resetView() {
        questionViewMap = emptyMap()
        snapshotProgress.visibility = View.GONE
    }

    private fun createQuizSubmission(silent: Boolean = false) {
        val quizCreationNetworkState = NetworkStateLiveData()

        if (view != null) {
            if (!silent) {
                showBlockingProgress()
            }

            quizCreationNetworkState
                .observeOnce(viewLifecycleOwner) {
                    when (it.code) {
                        NetworkCode.SUCCESS -> {
                            if (!silent) {
                                hideAnyProgress()
                                setSubmissionMode()
                            }
                            viewModel.onRefresh()
                            true
                        }

                        NetworkCode.NO_NETWORK -> {
                            hideAnyProgress()
                            showNetworkRequired()
                            true
                        }
                        else -> false
                    }
                }
        }

        viewModel.createQuizSubmission(quizCreationNetworkState)
    }

    private fun updateQuizSubmission(submission: QuizSubmission, submit: Boolean) {
        val quizSubmissionNetworkState = NetworkStateLiveData()

        if (view != null) {
            if (submit) {
                showBlockingProgress()
            }

            quizSubmissionNetworkState
                .observeOnce(viewLifecycleOwner) {
                    when (it.code) {
                        NetworkCode.SUCCESS -> {
                            if (submit) {
                                hideAnyProgress()
                                setSolutionMode()
                                viewModel.onRefresh()
                            } else {
                                snapshotProgress.visibility = View.INVISIBLE
                                snapshotCheckmark.visibility = View.VISIBLE
                                snapshotMessage.text = getString(R.string.quiz_snapshot_saved)
                            }
                            true
                        }
                        NetworkCode.NO_NETWORK -> {
                            if (submit) {
                                hideAnyProgress()
                                showNetworkRequired()
                            } else {
                                snapshotMessage.text =
                                    getString(R.string.quiz_snapshot_error_network)
                            }
                            true
                        }
                        NetworkCode.ERROR -> {
                            if (submit) {
                                hideAnyProgress()
                                showErrorMessage()
                            } else {
                                snapshotMessage.text = getString(R.string.quiz_snapshot_error)
                            }
                            true
                        }
                        else -> false
                    }
                }

            snapshotProgress.visibility = View.VISIBLE
            snapshotCheckmark.visibility = View.INVISIBLE
            snapshotMessage.text = getString(R.string.quiz_snapshot_saving)
        }

        viewModel.updateQuizSubmission(submission.apply {
            id = viewModel.quiz?.newestSubmissionId!!
            quizId = this@QuizFragment.quizId
            submitted = submit
        }, quizSubmissionNetworkState)
    }

    fun notifyActive() {
        updateActionButton()
    }

    private fun updateActionButton() {
        (activity as? CourseItemsActivity)?.let {
            if (isInSolutionMode) {
                it.updateActionButton(
                    this,
                    getString(R.string.quiz_redo),
                    getString(R.string.icon_reload)
                ) {
                    createQuizSubmission()
                    resetView()
                }
            } else {
                it.updateActionButton(
                    this,
                    getString(R.string.quiz_submit),
                    getString(R.string.icon_checkmark)
                ) {
                    stopSnapshotting()
                    updateQuizSubmission(getAnswers(questionViewMap), true)
                }
            }
        }
    }

    private fun setSolutionMode() {
        isInSolutionMode = true
        updateActionButton()
        submissionDetailsContainer.visibility = View.VISIBLE
        detailsContainer.visibility = View.GONE
        snapshotContainer.visibility = View.GONE
    }

    private fun setSubmissionMode() {
        isInSolutionMode = false
        updateActionButton()
        detailsContainer.visibility = View.VISIBLE
        submissionDetailsContainer.visibility = View.GONE
        snapshotContainer.visibility = View.VISIBLE
        startSnapshotting()
    }

    private fun lockQuestions(
        questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>>
    ) {
        questionViewMap.forEach { entry ->
            entry.value.second.lock()
        }
    }

    private fun unlockQuestions(
        questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>>
    ) {
        questionViewMap.forEach { entry ->
            entry.value.second.unlock()
        }
    }

    private fun insertAnswers(
        questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>>,
        submission: QuizSubmission
    ) {
        questionViewMap.forEach { entry ->
            val answer = submission.answers.find { it.questionId == entry.key }
            if (answer != null) {
                entry.value.second.insertAnswer(answer)
            }
        }
    }

    private fun showSolution(
        questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>>,
        submission: QuizSubmission
    ) {
        questionViewMap.forEach { entry ->
            val answer = submission.answers.find { it.questionId == entry.key }
            entry.value.second.showSolution(answer)
        }
    }

    private fun getAnswers(
        questionViewMap: Map<String, Pair<QuestionContainerView, QuestionView>>
    ): QuizSubmission {
        return QuizSubmission().apply {
            answers = RealmList()
            questionViewMap.forEach { entry ->
                val answer = entry.value.second.getAnswer()
                if (answer != null) {
                    answer.questionId = entry.key
                    answers.add(answer)
                }
            }
        }
    }

    private fun buildQuestions(
        questions: List<QuizQuestion>
    ): Map<String, Pair<QuestionContainerView, QuestionView>> {
        val map: MutableMap<String, Pair<QuestionContainerView, QuestionView>> = mutableMapOf()

        context?.let {
            questionContainer.removeAllViews()
            questions.sortedBy { it.position }.forEachIndexed { n, q ->
                val questionView = QuestionContainerView(it)

                questionView.numberView.text = getString(R.string.quiz_question_number, n + 1)
                questionView.pointsView.text = getString(R.string.quiz_question_points, q.maxPoints)
                questionView.questionView.setMarkdownText(q.text)

                when (q.type) {
                    QuizQuestion.TYPE_SELECT_ONE -> {
                        val view = SingleChoiceQuestionView(it)
                        view.shuffleOptions = q.shuffleOptions
                        view.options = q.options
                        view.changeListener = {
                            snapshot()
                        }
                        questionView.containerView.addView(view)

                        map[q.id!!] = Pair(questionView, view)
                    }
                    QuizQuestion.TYPE_SELECT_MULTIPLE -> {
                        val view = MultiChoiceQuestionView(it)
                        view.shuffleOptions = q.shuffleOptions
                        view.options = q.options
                        view.changeListener = {
                            snapshot()
                        }
                        questionView.containerView.addView(view)

                        map[q.id!!] = Pair(questionView, view)
                    }
                    QuizQuestion.TYPE_FREE_TEXT -> {
                        val view = FreeTextQuestionView(it)
                        view.option = q.options[0]!!
                        view.changeListener = {
                            snapshot()
                        }
                        questionView.containerView.addView(view)

                        map[q.id!!] = Pair(questionView, view)
                    }
                    else -> Unit
                }

                questionContainer.addView(questionView)
            }
        }

        return map
    }
}
