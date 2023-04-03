package de.xikolo.controllers.section

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.dialogs.OpenExternalContentDialog
import de.xikolo.controllers.dialogs.OpenExternalContentDialogAutoBundle
import de.xikolo.controllers.webview.WebViewActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.Item
import de.xikolo.models.LtiExercise
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.includeAuthToken
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.utils.extensions.showToast
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

    @BindView(R.id.attemptsIcon)
    lateinit var attemptsIcon: TextView

    @BindView(R.id.launch_button)
    lateinit var launchButton: Button

    override val layoutResource = R.layout.fragment_lti_exercise

    private val appPreferences = ApplicationPreferences()

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
            if (appPreferences.confirmOpenExternalContentLti) {
                val dialog = OpenExternalContentDialogAutoBundle.builder().type(Item.TYPE_LTI).build()
                dialog.listener = object : OpenExternalContentDialog.ExternalContentDialogListener {
                    override fun onOpen(dialog: DialogFragment) {
                        openExternalContent()
                    }

                    override fun onOpenAlways(dialog: DialogFragment) {
                        appPreferences.confirmOpenExternalContentLti = false
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
                ltiExercise = viewModel.ltiExercise

                updateView()
            }
    }

    private fun openExternalContent() {
        val title = item?.title ?: ""
        activity?.let {
            val intent = WebViewActivityAutoBundle.builder(title, ltiExercise!!.launchUrl)
                .externalLinksEnabled(true)
                .build(it)
            intent.includeAuthToken(UserManager.token!!)
            startActivity(intent)
        }
    }

    private fun updateView() {
        title.text = item?.title

        if (ltiExercise?.launchUrl == null) {
            hideAnyProgress()
            showMessage(
                R.string.not_available,
                R.string.lti_provider_deleted_summary
            )
            return
        }

        instructionsText.setMarkdownText(ltiExercise?.instructions)

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

        if (ltiExercise?.allowedAttempts == 0) {
            attemptsText.visibility = View.GONE
            attemptsIcon.visibility = View.GONE
        } else {
            attemptsText.visibility = View.VISIBLE
            attemptsIcon.visibility = View.VISIBLE
            attemptsText.text = getString(R.string.course_lti_allowed_attempts).format(ltiExercise?.allowedAttempts)
        }

        showContent()
    }

}
