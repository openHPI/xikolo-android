package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import de.xikolo.App
import de.xikolo.extensions.observe
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.viewmodels.base.BaseViewModel

abstract class MainFragment<T : BaseViewModel> : ViewModelFragment<T>() {

    var activityCallback: MainActivityCallback? = null

    override fun onStart() {
        super.onStart()
        try {
            activityCallback = activity as MainActivityCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement MainActivityCallback")
        }
    }

    override fun onStop() {
        super.onStop()
        activityCallback = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.instance.state.login
            .observe(viewLifecycleOwner) {
                onLoginStateChange(it)
            }
    }

    open fun onLoginStateChange(isLoggedIn: Boolean) {
        onRefresh()
    }

    protected fun showLoginRequired() {
        showLoginRequired {
            activityCallback?.selectDrawerSection(R.id.navigation_login)
        }
    }

}
