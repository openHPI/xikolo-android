package de.xikolo.views.quiz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.models.QuizQuestion
import de.xikolo.models.QuizQuestionOption
import de.xikolo.models.QuizSubmissionAnswer
import de.xikolo.utils.extensions.setMarkdownText

class MultiChoiceQuestionView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet), QuestionView {

    @BindView(R.id.options)
    lateinit var optionsView: LinearLayout

    var options: List<QuizQuestionOption> = mutableListOf()
        set(value) {
            field = value

            updateView()
        }

    var shuffleOptions: Boolean = false
        set(value) {
            field = value

            updateView()
        }

    private val optionViewMap: MutableMap<String, MultiChoiceOptionView> = mutableMapOf()

    override var changeListener: () -> Unit = {}

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.container_quiz_question_multi_choice, this, true)

        ButterKnife.bind(this)
    }

    private fun updateView() {
        optionViewMap.clear()
        optionsView.removeAllViews()

        val optionsOrdered =
            if (shuffleOptions) {
                options.shuffled()
            } else {
                options.sortedBy { it.position }
            }

        optionsOrdered.forEach {
            val view = MultiChoiceOptionView(context)
            view.text.setMarkdownText(it.text)
            view.option.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.isEnabled) {
                    changeListener()
                }
            }

            optionsView.addView(view)

            optionViewMap[it.id] = view
        }
    }

    override fun lock() {
        optionViewMap.forEach {
            it.value.option.apply {
                isEnabled = false
            }
        }
    }

    override fun unlock() {
        optionViewMap.forEach {
            it.value.option.apply {
                isEnabled = true
            }
        }
    }

    override fun showSolution(answer: QuizSubmissionAnswer?) {
        insertAnswer(answer)

        options
            .forEach {
                val optionView = optionViewMap[it.id]
                if (answer?.value?.data?.contains(it.id) == true) {
                    if (it.correct) {
                        optionView?.markCorrect()
                    } else {
                        optionView?.markWrong()
                    }
                } else if (it.correct) {
                    optionView?.markActual()
                }

                if (!it.explanation.isNullOrBlank()) {
                    optionView?.explanation?.text?.setMarkdownText(it.explanation)
                    optionView?.explanation?.visibility = View.VISIBLE
                }
            }
    }

    override fun getAnswer(): QuizSubmissionAnswer {
        val checkedIds = optionViewMap.filter {
            it.value.option.isChecked
        }.keys.toList()

        return QuizSubmissionAnswer("", QuizQuestion.TYPE_SELECT_MULTIPLE, checkedIds)
    }

    override fun insertAnswer(answer: QuizSubmissionAnswer?) {
        optionViewMap.forEach {
            it.value.option.isChecked = answer?.value?.data?.contains(it.key) == true
            // skip animation to avoid checked but empty boxes
            it.value.option.jumpDrawablesToCurrentState()
        }
    }
}
