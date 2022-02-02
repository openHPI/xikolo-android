package de.xikolo.controllers.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION_CODES.N
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.Config.WEBVIEW_LOGGING
import de.xikolo.controllers.webview.WebViewFragment
import de.xikolo.managers.UserManager
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.extensions.isOnline
import java.util.*

class WebViewHelper(view: View, private val webViewInterface: WebViewFragment) {

    companion object {
        val TAG = WebViewHelper::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var webView: WebView

    private var requestedUrl: String? = null

    init {
        ButterKnife.bind(this, view)

        setup()
    }

    private fun loadUrl(url: String, header: Map<String, String>?) {
        if (header != null) {
            webView.loadUrl(url, header)
        } else {
            webView.loadUrl(url)
        }
    }

    fun refresh() {
        request(requestedUrl)
    }

    fun requestedUrl(): String? {
        return requestedUrl
    }

    private fun setup() {
        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true

        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.userAgentString = Config.HEADER_USER_AGENT_VALUE

        webView.settings.domStorageEnabled = true

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                return !WEBVIEW_LOGGING || super.onConsoleMessage(cm)
            }
        }

        webView.webViewClient = object : WebViewClient() {

            @TargetApi(M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                @Suppress("DEPRECATION")
                onReceivedError(view, error?.errorCode
                    ?: -1, error?.description.toString(), request?.url.toString())
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                if (Config.DEBUG) Log.e(TAG, "An error occurred: $description")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                hideWebView()
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webViewInterface.hideAnyProgress()
                webViewInterface.hideMessage()
                showWebView()
                App.instance.state.connectivity.online()
                super.onPageFinished(view, url)
            }

            @TargetApi(N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                @Suppress("DEPRECATION")
                return shouldOverrideUrlLoading(view, request?.url.toString())
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.contains(Config.HOST) == true && url.contains("/auth/app")) {
                    val token = Uri.parse(url).getQueryParameter("token")
                    webViewInterface.interceptSSOLogin(token!!)
                } else if (url?.contains(Config.HOST) == true && webViewInterface.inAppLinksEnabled() || webViewInterface.externalLinksEnabled()) {
                    request(url)
                } else {
                    val uri = Uri.parse(url)
                    if (url?.contains(Config.HOST) == true && UserManager.isAuthorized) {
                        webViewInterface.openUrlInBrowser(uri, UserManager.token)
                    } else {
                        webViewInterface.openUrlInBrowser(uri, null)
                    }
                }
                return true
            }
        }

        webView.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()
                true
            } else {
                false
            }
        }
    }

    fun request(url: String?) {
        if (Config.DEBUG) {
            Log.d(TAG, "Request URL: " + url!!)
        }
        if (url != null) {
            this.requestedUrl = url

            if (!webViewInterface.externalLinksEnabled() || Patterns.WEB_URL.matcher(url).matches()) {
                if (App.instance.isOnline) {
                    webViewInterface.showAnyProgress()
                    if (url.contains(Config.HOST)) {
                        val header = HashMap<String, String>()
                        header[Config.HEADER_USER_PLATFORM] = Config.HEADER_USER_PLATFORM_VALUE
                        if (UserManager.isAuthorized) {
                            header[Config.HEADER_AUTH] = Config.HEADER_AUTH_VALUE_PREFIX + UserManager.token!!
                        }

                        // lanalytics context data cookie
                        val lanalyticsContextDataJson = LanalyticsUtil.contextDataJson
                        CookieManager.getInstance().setCookie(Config.HOST_URL, Config.LANALYTICS_CONTEXT_COOKIE + "=" + lanalyticsContextDataJson)

                        loadUrl(url, header)
                    } else {
                        loadUrl(url, null)
                    }
                } else {
                    webViewInterface.hideAnyProgress()
                    webViewInterface.showNetworkRequired()
                    App.instance.state.connectivity.offline()
                }
            } else {
                webViewInterface.hideAnyProgress()
                webViewInterface.showInvalidUrlToast()
            }
        } else {
            webViewInterface.hideAnyProgress()
        }
    }

    fun showWebView() {
        webView.visibility = View.VISIBLE
    }

    private fun hideWebView() {
        webView.visibility = View.GONE
    }

}
