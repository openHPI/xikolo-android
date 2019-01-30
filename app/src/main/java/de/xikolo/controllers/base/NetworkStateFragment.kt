package de.xikolo.controllers.base


import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import de.xikolo.R
import de.xikolo.controllers.helper.NetworkStateHelper
import de.xikolo.utils.NetworkUtil
import de.xikolo.utils.ToastUtil
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.base.NetworkCode
import de.xikolo.viewmodels.base.observe

abstract class NetworkStateFragment<T : BaseViewModel> : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, ViewModelCreationInterface<T> {

    private lateinit var networkStateHelper: NetworkStateHelper

    abstract val layoutResource: Int

    override lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate generic network state view
        val networkStateView = inflater.inflate(R.layout.fragment_loading_state, container, false) as ViewGroup
        // inflate content view inside
        val contentView = networkStateView.findViewById<ViewStub>(R.id.content_view)
        contentView.layoutResource = layoutResource
        contentView.inflate()
        // return complete view
        return networkStateView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    fun hideContent() {
        networkStateHelper.hideContent()
    }

    fun showContent() {
        networkStateHelper.showContent()
    }

    fun showBlockingProgress() {
        networkStateHelper.showBlockingProgress()
    }

    fun showAnyProgress() {
        networkStateHelper.showAnyProgress()
    }

    fun hideAnyProgress() {
        networkStateHelper.hideAnyProgress()
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

    fun showLoginRequired() {
        if (networkStateHelper.contentViewVisible) {
            ToastUtil.show(R.string.toast_please_log_in)
        } else {
            networkStateHelper.setMessageTitle(R.string.notification_please_login)
            networkStateHelper.setMessageSummary(R.string.notification_please_login_summary)
            networkStateHelper.showMessage()
        }
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

    fun hideMessage() {
        networkStateHelper.hideMessage()
    }

    fun showEmptyMessage(@StringRes title: Int, @StringRes summary: Int = 0) {
        if (!networkStateHelper.anyProgressVisible) {
            hideContent()
            networkStateHelper.setMessageTitle(title)
            networkStateHelper.setMessageSummary(summary)
            networkStateHelper.showMessage()
        }
    }

}
