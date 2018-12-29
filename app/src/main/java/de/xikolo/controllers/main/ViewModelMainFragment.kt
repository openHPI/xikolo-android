package de.xikolo.controllers.main

import android.support.v4.app.Fragment
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelMainFragment<T : BaseViewModel> : NetworkStateFragment<T>(), MainFragment {

    override var activityCallback: MainActivityCallback? = null

    override val fragment: Fragment
        get() = this

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

}
