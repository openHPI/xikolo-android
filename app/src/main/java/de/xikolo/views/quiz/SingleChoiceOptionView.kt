package de.xikolo.views.quiz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R

class SingleChoiceOptionView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    @BindView(R.id.option)
    lateinit var option: RadioButton

    @BindView(R.id.text)
    lateinit var text: TextView

    @BindView(R.id.indicator)
    lateinit var indicator: TextView

    @BindView(R.id.explanation)
    lateinit var explanation: ExplanationView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_quiz_question_single_choice, this, true)

        ButterKnife.bind(this)

        initViews()
    }

    fun markCorrect() {
        indicator.setText(R.string.icon_checkmark)
        indicator.setTextColor(ContextCompat.getColor(context, R.color.quiz_correct))
    }

    fun markWrong() {
        indicator.setText(R.string.icon_cross)
        indicator.setTextColor(ContextCompat.getColor(context, R.color.quiz_wrong))
    }

    fun markActual() {
        indicator.setText(R.string.icon_checkmark)
        indicator.setTextColor(ContextCompat.getColor(context, R.color.quiz_actual))
    }

    private fun initViews() {
        text.setOnClickListener {
            option.performClick()
        }
    }
}
