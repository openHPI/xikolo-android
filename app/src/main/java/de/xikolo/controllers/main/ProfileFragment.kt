package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.GlideApp
import de.xikolo.controllers.webview.WebViewActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.models.User
import de.xikolo.viewmodels.main.ProfileViewModel

class ProfileFragment : MainFragment<ProfileViewModel>() {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName
    }

    @BindView(R.id.textFullName)
    lateinit var textFullName: TextView

    @BindView(R.id.textName)
    lateinit var textName: TextView

    @BindView(R.id.imageProfile)
    lateinit var imageProfile: ImageView

    @BindView(R.id.textEnrollCount)
    lateinit var textEnrollCounts: TextView

    @BindView(R.id.textEmail)
    lateinit var textEmail: TextView

    @BindView(R.id.buttonEditProfile)
    lateinit var buttonEditProfile: Button

    override val layoutResource = R.layout.fragment_profile

    override fun createViewModel(): ProfileViewModel {
        return ProfileViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user
            .observe(viewLifecycleOwner) {
                showUser(it)
                showContent()
            }

        viewModel.enrollments
            .observe(viewLifecycleOwner) {
                updateEnrollmentCount(viewModel.enrollmentCount)
            }
    }

    private fun showUser(user: User) {
        activityCallback?.onFragmentAttached(
            R.id.navigation_login,
            getString(R.string.title_section_profile)
        )

        if (user.name == user.profile.fullName) {
            textFullName.text = user.name
            textName.visibility = View.GONE
        } else {
            textFullName.text = user.profile.fullName
            textName.text = user.name
        }

        textEmail.text = user.profile.email

        buttonEditProfile.setOnClickListener {
            activity?.let {
                val url = Config.HOST_URL + Config.PROFILE

                val intent = WebViewActivityAutoBundle
                    .builder(getString(R.string.btn_edit_profile), url)
                    .inAppLinksEnabled(false)
                    .externalLinksEnabled(false)
                    .build(it)

                startActivity(intent)
            }
        }

        GlideApp.with(App.instance)
            .load(user.avatarUrl)
            .circleCrop()
            .allPlaceholders(R.drawable.avatar_placeholder)
            .into(imageProfile)
    }

    private fun updateEnrollmentCount(count: Long) {
        textEnrollCounts.text = count.toString()
    }

    override fun onLoginStateChange(isLoggedIn: Boolean) {
        if (!isLoggedIn) {
            viewModel.user.removeObservers(viewLifecycleOwner)
            viewModel.enrollments.removeObservers(viewLifecycleOwner)
            if (isAdded) {
                parentFragmentManager.popBackStack()
            }
        }
    }
}
