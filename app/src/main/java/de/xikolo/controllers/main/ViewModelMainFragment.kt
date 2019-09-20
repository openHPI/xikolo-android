package de.xikolo.controllers.main

import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelMainFragment<T : BaseViewModel> : ViewModelFragment<T>() {

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

    protected fun showLoginRequired() {
        showLoginRequired {
            activityCallback?.selectDrawerSection(R.id.navigation_login)
        }
    }

}
