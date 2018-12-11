package de.xikolo.controllers.main

import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelMainFragment<T : BaseViewModel> : NetworkStateFragment<T>() {

    protected var activityCallback: MainFragment.MainActivityCallback? = null // ToDo change later

    override fun onStart() {
        super.onStart()
        try {
            activityCallback = activity as MainFragment.MainActivityCallback? // ToDo change later
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement MainActivityCallback")
        }

    }

    override fun onStop() {
        super.onStop()
        activityCallback = null
    }

}
