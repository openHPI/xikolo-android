package de.xikolo.controllers.main

import de.xikolo.controllers.base.LoadingStatePresenterFragment
import de.xikolo.presenters.base.LoadingStatePresenter
import de.xikolo.presenters.base.LoadingStateView

abstract class PresenterMainFragment<P : LoadingStatePresenter<V>, V : LoadingStateView> : LoadingStatePresenterFragment<P, V>() {

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

}
