package de.xikolo.views.quiz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.models.QuizQuestion
import de.xikolo.models.QuizQuestionOption
import de.xikolo.models.QuizSubmissionAnswer
import de.xikolo.utils.extensions.setMarkdownText
import java.util.Locale

class FreeTextQuestionView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet), QuestionView {

    @BindView(R.id.textField)
    lateinit var textField: EditText

    @BindView(R.id.indicator)
    lateinit var indicator: TextView

    @BindView(R.id.explanation)
    lateinit var explanation: ExplanationView

    lateinit var option: QuizQuestionOption

    override var changeListener: () -> Unit = {}

    init {
        LayoutInflater.from(context).inflate(R.layout.container_quiz_question_free_text, this, true)

        ButterKnife.bind(this)

        textField.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (v.isEnabled && hasFocus) {
                changeListener
            }
        }
    }

    private fun markCorrect() {
        indicator.setText(R.string.icon_checkmark)
        indicator.setTextColor(ContextCompat.getColor(context, R.color.quiz_correct))
    }

    private fun markWrong() {
        indicator.setText(R.string.icon_cross)
        indicator.setTextColor(ContextCompat.getColor(context, R.color.quiz_wrong))
    }

    override fun lock() {
        textField.isEnabled = false
    }

    override fun unlock() {
        textField.isEnabled = true
    }

    override fun showSolution(answer: QuizSubmissionAnswer?) {
        if (answer?.value?.data?.first()?.toLowerCase(Locale.getDefault())
                ?.trim() == option.text.toLowerCase(Locale.getDefault()).trim()
        ) {
            markCorrect()
        } else {
            markWrong()
        }

        if (!option.explanation.isNullOrBlank()) {
            explanation.text.setMarkdownText(option.explanation)
            explanation.visibility = View.VISIBLE
        }
    }

    override fun getAnswer(): QuizSubmissionAnswer {
        return QuizSubmissionAnswer("", QuizQuestion.TYPE_FREE_TEXT, textField.text.toString())
    }

    override fun insertAnswer(answer: QuizSubmissionAnswer?) {
        textField.setText(answer?.value?.data?.first())
    }
}
