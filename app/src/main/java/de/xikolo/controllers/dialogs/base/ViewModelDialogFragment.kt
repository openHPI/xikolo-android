package de.xikolo.controllers.dialogs.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelCreationInterface
import de.xikolo.controllers.helper.NetworkStateHelper
import de.xikolo.extensions.observe
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.utils.NetworkUtil
import de.xikolo.utils.ToastUtil
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelDialogFragment<T : BaseViewModel> : BaseDialogFragment(), SwipeRefreshLayout.OnRefreshListener, ViewModelCreationInterface<T> {

    private lateinit var networkStateHelper: NetworkStateHelper

    override lateinit var viewModel: T

    abstract val layoutResource: Int

    protected lateinit var dialogView: View
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this)

        dialogView = createView(LayoutInflater.from(activity), null, savedInstanceState)

        onDialogViewCreated(dialogView, savedInstanceState)
    }

    private fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // inflate generic network state view
        val networkStateView = inflater.inflate(R.layout.fragment_loading_state, container, false) as ViewGroup
        // inflate content view inside
        val contentView = networkStateView.findViewById<ViewStub>(R.id.content_view)
        contentView.layoutResource = layoutResource
        contentView.inflate()
        // return complete view
        return networkStateView
    }

    open fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        ButterKnife.bind(this, view)

        networkStateHelper = NetworkStateHelper(activity, view, this)

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

    fun showContent() {
        networkStateHelper.showContent()
    }

    fun showAnyProgress() {
        networkStateHelper.showAnyProgress()
    }

    fun hideAnyProgress() {
        networkStateHelper.hideAnyProgress()
    }

    fun showErrorMessage() {
        if (networkStateHelper.contentViewVisible) {
            ToastUtil.show(R.string.error)
        } else {
            networkStateHelper.setMessageTitle(R.string.error)
            networkStateHelper.setMessageSummary(null)
            networkStateHelper.showMessage()
        }
    }

    fun showNetworkRequired() {
        if (networkStateHelper.contentViewVisible) {
            NetworkUtil.showNoConnectionToast()
        } else {
            networkStateHelper.setMessageTitle(R.string.notification_no_network)
            networkStateHelper.setMessageSummary(R.string.notification_no_network_summary)
            networkStateHelper.showMessage()
        }
    }

}
