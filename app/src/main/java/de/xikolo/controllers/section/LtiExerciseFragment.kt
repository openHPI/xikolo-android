package de.xikolo.controllers.section

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.models.Item
import de.xikolo.models.LtiExercise
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.viewmodels.section.LtiExerciseViewModel

class LtiExerciseFragment : ViewModelFragment<LtiExerciseViewModel>() {

    companion object {
        val TAG: String = LtiExerciseFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.instructions)
    lateinit var instructionsText: TextView

    @BindView(R.id.graded)
    lateinit var gradingText: TextView

    @BindView(R.id.points)
    lateinit var pointsText: TextView

    @BindView(R.id.attempts)
    lateinit var attemptsText: TextView

    @BindView(R.id.launch_button)
    lateinit var launchButton: Button

    override val layoutResource = R.layout.fragment_lti_exercise

    private var item: Item? = null

    private var ltiExercise: LtiExercise? = null

    override fun createViewModel(): LtiExerciseViewModel {
        return LtiExerciseViewModel(itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchButton.setOnClickListener {
            Uri.parse(ltiExercise?.launchUrl)?.let { url ->
                val intent = Intent(ACTION_VIEW)
                intent.data = url
                startActivity(intent)
            }
        }

        viewModel.item
            .observe(viewLifecycleOwner) {
                item = it
                ltiExercise = viewModel.ltiExercise

                updateView()
            }
    }

    private fun updateView() {
        title.text = item?.title
        instructionsText.setMarkdownText(ltiExercise?.instructions)
        gradingText.text = getString(
            when (item?.exerciseType) { //ToDo
                Item.EXERCISE_TYPE_MAIN     -> R.string.course_lti_graded
                Item.EXERCISE_TYPE_SELFTEST -> R.string.course_lti_ungraded
                else                        -> R.string.course_lti_ungraded
            }
        )
        pointsText.text = getString(R.string.course_lti_points).format(item?.maxPoints)
        attemptsText.text =
            if (ltiExercise?.allowedAttempts == 0) {
                getString(R.string.course_lti_allowed_attempts_infinite)
            } else {
                getString(R.string.course_lti_allowed_attempts).format(ltiExercise?.allowedAttempts)
            }
        // ToDo what about weight, time effort, accessible, deadline, time_effort? Is it important?
        showContent()
    }

}
