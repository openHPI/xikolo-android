package de.xikolo.controllers.base


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import de.xikolo.R
import de.xikolo.controllers.helper.LoadingStateHelper
import de.xikolo.lifecycle.base.BaseViewModel
import de.xikolo.lifecycle.base.NetworkCode
import de.xikolo.presenters.base.LoadingStateView
import de.xikolo.utils.NetworkUtil
import de.xikolo.utils.ToastUtil

abstract class NetworkStateFragment<T : BaseViewModel> : BaseFragment(), LoadingStateView, SwipeRefreshLayout.OnRefreshListener {

    private var loadingStateHelper: LoadingStateHelper? = null

    protected val viewModel: T by lazy {
        ViewModelProviders.of(this, getViewModelFactory()).get(viewModel.javaClass)
    }

    private fun getViewModelFactory(): ViewModelProvider.NewInstanceFactory {
        return object : ViewModelProvider.NewInstanceFactory() {
            override fun <S : ViewModel?> create(modelClass: Class<S>): S {
                @Suppress("unchecked_cast")
                return createViewModel() as S
            }
        }
    }

    abstract fun createViewModel(): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate generic loading state view
        val loadingStateView = inflater.inflate(R.layout.fragment_loading_state, container, false) as ViewGroup
        // inflate content view inside
        val contentView = loadingStateView.findViewById<ViewStub>(R.id.content_view)
        contentView.layoutResource = layoutResource
        contentView.inflate()
        // return complete view
        return loadingStateView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingStateHelper = LoadingStateHelper(activity, view, this)

        viewModel.networkState.observe(this, Observer {
            hideProgress()
            when (it?.code) {
                NetworkCode.SUCCESS                   -> hideProgress()
                NetworkCode.NO_NETWORK                -> if (it.userRequest || !contentViewVisible) showNetworkRequiredMessage()
                NetworkCode.ERROR, NetworkCode.CANCEL -> showErrorMessage()
                else                                  -> {}
            }
        })

        viewModel.onCreate()
    }

    override fun onRefresh() {
        viewModel.onRefresh()
    }

    override fun hideContent() {
        loadingStateHelper?.hideContentView()
    }

    override fun showContent() {
        loadingStateHelper?.showContentView()
    }

    override fun showBlockingProgress() {
        loadingStateHelper?.showBlockingProgress()
    }

    override fun showProgress() {
        loadingStateHelper?.showProgress()
    }

    override fun hideProgress() {
        loadingStateHelper?.hideProgress()
    }

    override fun showNetworkRequiredMessage() {
        loadingStateHelper?.let {
            if (it.isContentViewVisible) {
                NetworkUtil.showNoConnectionToast()
            } else {
                it.setMessageTitle(R.string.notification_no_network)
                it.setMessageSummary(R.string.notification_no_network_summary)
                it.showMessage()
            }
        }
    }

    override fun showLoginRequiredMessage() {
        loadingStateHelper?.let {
            if (it.isContentViewVisible) {
                ToastUtil.show(R.string.toast_please_log_in)
            } else {
                it.setMessageTitle(R.string.notification_please_login)
                it.setMessageSummary(R.string.notification_please_login_summary)
                it.showMessage()
            }
        }
    }

    override fun showErrorMessage() {
        loadingStateHelper?.let {
            if (it.isContentViewVisible) {
                ToastUtil.show(R.string.error)
            } else {
                it.setMessageTitle(R.string.error)
                it.setMessageSummary(null)
                it.showMessage()
            }
        }
    }

    override fun hideMessage() {
        loadingStateHelper?.hideMessage()
    }

    override fun isContentViewVisible(): Boolean {
        return contentViewVisible
    }

    val contentViewVisible: Boolean
        get() = loadingStateHelper?.isContentViewVisible == true

}
