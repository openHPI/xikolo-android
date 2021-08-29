package de.xikolo.controllers.webview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.helper.WebViewHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.utils.extensions.includeAuthToken
import de.xikolo.utils.extensions.isOnline
import de.xikolo.utils.extensions.showToast
import de.xikolo.views.NestedScrollWebView

class WebViewFragment : NetworkStateFragment() {

    companion object {
        val TAG: String = WebViewFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var url: String

    @AutoBundleField(required = false)
    var inAppLinksEnabled: Boolean = false

    @AutoBundleField(required = false)
    var externalLinksEnabled: Boolean = false

    @AutoBundleField(required = false)
    var allowBack: Boolean = false

    private var webViewHelper: WebViewHelper? = null

    override val layoutResource: Int
        get() = R.layout.fragment_webview

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webViewHelper?.webView?.saveState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentView = view.findViewById<NestedScrollWebView>(R.id.content_view)

        webViewHelper = WebViewHelper(contentView!!, this)

        if (!context.isOnline) {
            showNetworkRequired()
        } else if (webViewHelper?.requestedUrl() == null) {
            if (savedInstanceState == null) {
                webViewHelper?.request(url)
            } else {
                webViewHelper?.webView?.restoreState(savedInstanceState)
            }
        } else {
            webViewHelper?.showWebView()
        }
    }

    fun onBack(): Boolean {
        val canGoBack = allowBack && webViewHelper?.webView?.canGoBack() == true
        if (canGoBack) {
            webViewHelper?.webView?.goBack()
        }
        return canGoBack
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                webViewHelper?.refresh()
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
        val intent = LoginActivityAutoBundle.builder().token(token).build(requireActivity())
        activity?.startActivity(intent)
    }

    override fun onRefresh() {
        if (webViewHelper?.requestedUrl() != null) {
            webViewHelper?.refresh()
        } else {
            webViewHelper?.request(url)
        }
    }
}
