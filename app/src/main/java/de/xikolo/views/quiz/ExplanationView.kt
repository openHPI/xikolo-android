package de.xikolo.views.quiz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R

class ExplanationView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    @BindView(R.id.toggle)
    lateinit var toggle: Button

    @BindView(R.id.text)
    lateinit var text: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_quiz_question_explanation, this, true)

        ButterKnife.bind(this)

        initViews()
    }

    private fun initViews() {
        toggle.setOnClickListener {
            if (text.visibility == View.VISIBLE) {
                text.visibility = View.GONE
                toggle.text = context.getString(R.string.quiz_question_explanation_show)
            } else {
                text.visibility = View.VISIBLE
                toggle.text = context.getString(R.string.quiz_question_explanation_hide)
            }
        }
    }
}
