package de.xikolo.controllers.section

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.dialogs.OpenExternalContentDialog
import de.xikolo.controllers.dialogs.OpenExternalContentDialogAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.Item
import de.xikolo.models.PeerAssessment
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.createChooser
import de.xikolo.utils.extensions.includeAuthToken
import de.xikolo.utils.extensions.isPast
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.utils.extensions.showToast
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

    @BindView(R.id.launch_button)
    lateinit var launchButton: Button

    override val layoutResource = R.layout.fragment_peer_assessment

    private var item: Item? = null

    private var peerAssessment: PeerAssessment? = null

    private val appPreferences = ApplicationPreferences()

    override fun createViewModel(): PeerAssessmentViewModel {
        return PeerAssessmentViewModel(itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchButton.setOnClickListener {
            if (appPreferences.confirmOpenExternalContentPeer) {
                val dialog = OpenExternalContentDialogAutoBundle.builder().type(Item.TYPE_PEER).build()
                dialog.listener = object : OpenExternalContentDialog.ExternalContentDialogListener {
                    override fun onOpen(dialog: DialogFragment) {
                        openExternalContent()
                    }

                    override fun onOpenAlways(dialog: DialogFragment) {
                        appPreferences.confirmOpenExternalContentPeer = false
                        openExternalContent()
                    }
                }
                activity?.let {
                    dialog.show(it.supportFragmentManager, OpenExternalContentDialog.TAG)
                }
            } else {
                openExternalContent()
            }

        }

        viewModel.item
            .observe(viewLifecycleOwner) {
                item = it
                peerAssessment = viewModel.peerAssessment

                updateView()
            }
    }

    private fun openExternalContent() {
        peerAssessment?.let {
            val url = "${Config.HOST_URL}peer_assessments/${it.id}"
            val customTabsIntent = CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(App.instance, R.color.apptheme_primary))
                .build()

            val intent = customTabsIntent.intent.apply {
                data = Uri.parse(url)
                includeAuthToken(UserManager.token!!)
            }
            context?.let { context ->
                intent.createChooser(context, null, true)?.let { intent ->
                    startActivity(intent)
                } ?: run {
                    showToast(R.string.error_plain)
                }
            }
        } ?: run {
            showToast(R.string.error_plain)
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
