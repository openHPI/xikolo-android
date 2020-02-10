package de.xikolo.controllers.webview

import android.content.Intent
import android.content.MutableContextWrapper
import android.net.Uri
import android.os.Bundle
import android.view.*
import com.crashlytics.android.Crashlytics
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.helper.WebViewHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.utils.extensions.includeAuthToken
import de.xikolo.utils.extensions.isOnline
import de.xikolo.utils.extensions.showToast

class WebViewFragment : NetworkStateFragment() {

    companion object {
        val TAG = WebViewFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var url: String

    @AutoBundleField(required = false)
    var inAppLinksEnabled: Boolean = false

    @AutoBundleField(required = false)
    var externalLinksEnabled: Boolean = false

    private var contentView: View? = null

    private lateinit var webViewHelper: WebViewHelper
    private var mutableContextWrapper: MutableContextWrapper? = null

    override val layoutResource: Int
        get() = R.layout.fragment_webview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (contentView == null) {
            mutableContextWrapper = MutableContextWrapper(activity)

            contentView = LayoutInflater.from(mutableContextWrapper).inflate(R.layout.fragment_loading_state, container, false)
            // inflate content contentView inside
            val contentView = contentView?.findViewById<ViewStub>(R.id.content_view)
            contentView?.layoutResource = layoutResource
            contentView?.inflate()

            webViewHelper = WebViewHelper(this.contentView!!, this)
        } else {
            mutableContextWrapper?.baseContext = activity
        }

        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!context.isOnline) {
            showNetworkRequired()
        } else if (webViewHelper.requestedUrl() == null) {
            webViewHelper.request(url)
        } else {
            webViewHelper.showWebView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (retainInstance && contentView?.parent is ViewGroup) {
            try {
                (contentView?.parent as ViewGroup).removeView(contentView)
                mutableContextWrapper?.baseContext = App.instance
            } catch (e: Exception) {
                Crashlytics.logException(e)
                contentView = null
                mutableContextWrapper = null
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                webViewHelper.refresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showInvalidUrlToast() {
        this.showToast(R.string.notification_url_invalid)
    }

    fun inAppLinksEnabled(): Boolean {
        return inAppLinksEnabled
    }

    fun externalLinksEnabled(): Boolean {
        return externalLinksEnabled
    }

    fun openUrlInBrowser(uri: Uri, token: String?) {
        val i = Intent(Intent.ACTION_VIEW, uri)
        if (token != null) {
            i.includeAuthToken(token)
        }
        activity?.startActivity(i)
    }

    fun interceptSSOLogin(token: String) {
        val intent = LoginActivityAutoBundle.builder().token(token).build(activity!!)
        activity?.startActivity(intent)
    }

    override fun onRefresh() {
        if (webViewHelper.requestedUrl() != null) {
            webViewHelper.refresh()
        } else {
            webViewHelper.request(url)
        }
    }
}
