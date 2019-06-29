package de.xikolo.controllers.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.managers.UserManager
import de.xikolo.models.User
import de.xikolo.utils.DisplayUtil
import de.xikolo.viewmodels.main.ProfileViewModel
import de.xikolo.views.CustomSizeImageView

class ProfileFragment : ViewModelMainFragment<ProfileViewModel>() {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName
    }

    @BindView(R.id.textName)
    lateinit var textName: TextView

    @BindView(R.id.imageHeader)
    lateinit var imageHeader: CustomSizeImageView

    @BindView(R.id.imageProfile)
    lateinit var imageProfile: CustomSizeImageView

    @BindView(R.id.textEnrollCount)
    lateinit var textEnrollCounts: TextView

    @BindView(R.id.textEmail)
    lateinit var textEmail: TextView

    override val layoutResource = R.layout.content_profile

    override fun createViewModel(): ProfileViewModel {
        return ProfileViewModel()
    }

    override fun onStart() {
        super.onStart()

        if (!UserManager.isAuthorized) {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user
            .observe(this, Observer {
                showUser(it)
                showContent()
            })

        viewModel.enrollments
            .observe(this, Observer {
                updateEnrollmentCount(viewModel.enrollmentCount)
            })
    }

    private fun showUser(user: User) {
        val userTitle = String.format(
            getString(R.string.user_name),
            user.profile.firstName,
            user.profile.lastName
        )

        activityCallback?.onFragmentAttached(NavigationAdapter.NAV_PROFILE.position, userTitle)

        textName.text = userTitle
        textEmail.text = user.profile.email


        val size = DisplayUtil.getDisplaySize(activity)
        val heightHeader: Int
        val heightProfile: Int
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            heightHeader = (size.y * 0.2).toInt()
            heightProfile = (size.x * 0.2).toInt()
        } else {
            heightHeader = (size.y * 0.35).toInt()
            heightProfile = (size.y * 0.2).toInt()
        }
        imageHeader.setDimensions(size.x, heightHeader)
        imageHeader.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        imageProfile.setDimensions(heightProfile, heightProfile)
        imageProfile.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        GlideApp.with(this).load(R.drawable.title).into(imageHeader)

        GlideApp.with(App.instance)
            .load(user.avatarUrl)
            .circleCrop()
            .allPlaceholders(R.drawable.avatar)
            .override(heightProfile)
            .into(imageProfile)

        val layoutParams = imageProfile.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(0, imageHeader.measuredHeight - imageProfile.measuredHeight / 2, 0, 0)
        imageProfile.layoutParams = layoutParams
    }

    private fun updateEnrollmentCount(count: Long) {
        textEnrollCounts.text = count.toString()
    }

}
