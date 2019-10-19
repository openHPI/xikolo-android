package de.xikolo.controllers.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle
import de.xikolo.utils.extensions.showToast
import de.xikolo.views.CustomFontTextView

class NetworkStateHelper(private val activity: FragmentActivity?, view: View, onRefreshListener: SwipeRefreshLayout.OnRefreshListener) {

    @BindView(R.id.content_view)
    lateinit var contentView: View

    @BindView(R.id.container_content_message)
    lateinit var messageContainer: FrameLayout

    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @BindView(R.id.text_notification_symbol)
    lateinit var textIcon: CustomFontTextView

    @BindView(R.id.text_notification_header)
    lateinit var textHeader: TextView

    @BindView(R.id.text_notification_summary)
    lateinit var textSummary: TextView

    private var progressDialog: ProgressDialogIndeterminate? = null

    val contentViewVisible: Boolean
        get() = contentView.visibility == View.VISIBLE

    val messageVisible: Boolean
        get() = messageContainer.visibility == View.VISIBLE

    val anyProgressVisible: Boolean
        get() = refreshLayout.isRefreshing || progressBar.visibility == View.VISIBLE

    val messageSymbol: CharSequence
        get() = textIcon.text

    val messageTitle: CharSequence
        get() = textHeader.text

    val messageSummary: CharSequence
        get() = textSummary.text

    init {
        ButterKnife.bind(this, view)

        refreshLayout.setColorSchemeResources(
            R.color.apptheme_second,
            R.color.apptheme_main
        )
        refreshLayout.setOnRefreshListener(onRefreshListener)

        hideContent()
        hideMessage()
        hideAnyProgress()
    }

    fun showContent() {
        hideMessage()
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
            refreshLayout.isRefreshing = true
        }
        contentView.visibility = View.VISIBLE
    }

    fun hideContent() {
        contentView.visibility = View.GONE
    }

    fun showAnyProgress() {
        if (contentViewVisible || messageVisible) {
            if (!refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = true
            }
        } else {
            progressBar.visibility = View.VISIBLE
        }
    }

    fun showBlockingProgress() {
        activity?.let {
            progressDialog = ProgressDialogIndeterminateAutoBundle.builder().build()
            progressDialog?.show(it.supportFragmentManager, ProgressDialogIndeterminate.TAG)
        }
    }

    fun hideAnyProgress() {
        refreshLayout.isRefreshing = false
        progressBar.visibility = View.GONE

        progressDialog?.let {
            if (it.dialog?.isShowing == true) {
                it.dismiss()
            }
        }
    }

    fun showMessage() {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
            refreshLayout.isRefreshing = true
        }
        messageContainer.visibility = View.VISIBLE
    }

    fun hideMessage() {
        messageContainer.visibility = View.GONE
    }

    fun setMessageOnClickListener(listener: View.OnClickListener) {
        messageContainer.setOnClickListener(listener)
    }

    fun setMessageSymbol(symbol: String) {
        textIcon.text = symbol
    }

    fun setMessageSymbol(title: Int) {
        textIcon.text = App.instance.resources.getString(title)
    }

    fun setMessageTitle(title: String) {
        textHeader.text = title
    }

    fun setMessageTitle(title: Int) {
        textHeader.text = App.instance.resources.getString(title)
    }

    fun setMessageSummary(summary: String?) {
        if (summary == null) {
            textSummary.visibility = View.GONE
        } else {
            textSummary.visibility = View.VISIBLE
        }
        textSummary.text = summary
    }

    fun setMessageSummary(summary: Int) {
        if (summary == 0) {
            textSummary.text = null
            textSummary.visibility = View.GONE
        } else {
            textSummary.text = App.instance.resources.getString(summary)
            textSummary.visibility = View.VISIBLE
        }
    }

    fun enableSwipeRefresh(enabled: Boolean) {
        refreshLayout.isEnabled = enabled
    }


    interface NetworkStateOwner {

        val networkStateHelper: NetworkStateHelper

        fun inflateHelper(inflater: LayoutInflater, container: ViewGroup?, layoutResource: Int): View {
            val networkStateView = inflater.inflate(R.layout.fragment_loading_state, container, false) as ViewGroup

            val contentView = networkStateView.findViewById<ViewStub>(R.id.content_view)
            contentView.layoutResource = layoutResource
            contentView.inflate()

            return networkStateView
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
                networkStateHelper.activity?.showToast(R.string.toast_no_network)
            } else {
                networkStateHelper.setMessageTitle(R.string.notification_no_network)
                networkStateHelper.setMessageSummary(R.string.notification_no_network_summary)
                networkStateHelper.showMessage()
            }
        }

        fun showLoginRequired(onClick: () -> Unit = {}) {
            if (networkStateHelper.contentViewVisible) {
                networkStateHelper.activity?.showToast(R.string.toast_please_log_in)
            } else {
                networkStateHelper.setMessageTitle(R.string.notification_please_login)
                networkStateHelper.setMessageSummary(R.string.notification_please_login_summary)
                networkStateHelper.setMessageOnClickListener(View.OnClickListener { onClick() })
                networkStateHelper.showMessage()
            }
        }

        fun showErrorMessage() {
            if (networkStateHelper.contentViewVisible) {
                networkStateHelper.activity?.showToast(R.string.error)
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

        fun showMessage(@StringRes title: Int, @StringRes summary: Int, onClick: () -> Unit = {}) {
            if (networkStateHelper.contentViewVisible) {
                networkStateHelper.activity?.showToast(title)
            } else {
                networkStateHelper.setMessageTitle(title)
                networkStateHelper.setMessageSummary(summary)
                networkStateHelper.setMessageOnClickListener(View.OnClickListener { onClick() })
                networkStateHelper.showMessage()
            }
        }
    }
}
