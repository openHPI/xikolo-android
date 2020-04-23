package de.xikolo.views.quiz

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R

class QuestionContainerView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    @BindView(R.id.number)
    lateinit var numberView: TextView

    @BindView(R.id.points)
    lateinit var pointsView: TextView

    @BindView(R.id.question)
    lateinit var questionView: TextView

    @BindView(R.id.explanation)
    lateinit var explanationView: ExplanationView

    @BindView(R.id.container)
    lateinit var containerView: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.container_quiz_question, this, true)

        ButterKnife.bind(this)
    }
}
