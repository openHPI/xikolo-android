package de.xikolo.controllers.dialogs.base

import android.os.Bundle
import android.view.View
import de.xikolo.controllers.base.ViewModelCreationInterface
import de.xikolo.extensions.observe
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelDialogFragment<T : BaseViewModel> : NetworkStateDialogFragment(), ViewModelCreationInterface<T> {

    override lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this)
    }

    override fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onDialogViewCreated(view, savedInstanceState)

        viewModel.networkState.observe(this) {
            if (it.code != NetworkCode.STARTED) hideAnyProgress()
            when (it.code) {
                NetworkCode.STARTED                   -> showAnyProgress()
                NetworkCode.NO_NETWORK                -> if (it.userRequest || !networkStateHelper.contentViewVisible) showNetworkRequired()
                NetworkCode.ERROR, NetworkCode.CANCEL -> showErrorMessage()
                else                                  -> Unit
            }
        }

        viewModel.onCreate()
    }

    override fun onRefresh() {
        viewModel.onRefresh()
    }

}
