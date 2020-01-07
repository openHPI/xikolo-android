package de.xikolo.controllers.section

import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.models.Item
import de.xikolo.models.PeerAssessment
import de.xikolo.utils.extensions.isPast
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.viewmodels.section.PeerAssessmentViewModel

class PeerAssessmentFragment : ViewModelFragment<PeerAssessmentViewModel>() {

    companion object {
        val TAG: String = PeerAssessmentFragment::class.java.simpleName
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

    @BindView(R.id.type)
    lateinit var typeText: TextView

    @BindView(R.id.typeIcon)
    lateinit var typeIcon: TextView

    override val layoutResource = R.layout.fragment_peer_assessment

    private var item: Item? = null

    private var peerAssessment: PeerAssessment? = null

    override fun createViewModel(): PeerAssessmentViewModel {
        return PeerAssessmentViewModel(itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.item
            .observe(viewLifecycleOwner) {
                item = it
                peerAssessment = viewModel.peerAssessment

                updateView()
            }
    }

    private fun updateView() {
        title.text = item?.title

        instructionsText.setMarkdownText(peerAssessment?.instructions)

        gradingText.text = getString(
            when (item?.exerciseType) {
                Item.EXERCISE_TYPE_MAIN     -> R.string.course_lti_graded
                Item.EXERCISE_TYPE_SELFTEST -> R.string.course_lti_ungraded
                Item.EXERCISE_TYPE_BONUS    -> R.string.course_lti_bonus
                else                        -> R.string.course_lti_ungraded
            }
        )
        if (item?.exerciseType == Item.EXERCISE_TYPE_SURVEY) {
            gradingText.visibility = View.GONE
        }

        pointsText.text = getString(R.string.course_lti_points).format(item?.maxPoints)

        if (peerAssessment?.type == PeerAssessment.TYPE_SOLO) {
            typeText.text = getString(R.string.course_peer_type_solo)
            typeIcon.text = getString(R.string.icon_solo)
        } else {
            typeText.text = getString(R.string.course_peer_type_team)
            typeIcon.text = getString(R.string.icon_team)
        }

        if (item?.deadline.isPast) {
            hideContent()
            showMessage(R.string.notification_submission_deadline_surpassed, R.string.notification_submission_deadline_surpassed_summary)
        } else {
            showContent()
        }
    }

}
