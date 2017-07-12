package de.xikolo.controllers.helper;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.controllers.webview.WebViewInterface;
import de.xikolo.managers.UserManager;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

import static de.xikolo.config.Config.WEBVIEW_LOGGING;

public class WebViewHelper {

    public static final String TAG = WebViewHelper.class.getSimpleName();

    @BindView(R.id.webView) WebView webView;

    protected String url;

    private WebViewInterface webViewInterface;

    public WebViewHelper(View view, WebViewInterface callback) {
        ButterKnife.bind(this, view);
        webViewInterface = callback;

        setup();
    }

    private void loadUrl(String url, Map<String, String> header) {
        webView.loadUrl(url, header);
    }

    private boolean webViewIsShown() {
        return webView.getVisibility() == View.VISIBLE;
    }

    private void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    private void hideWebView() {
        webView.setVisibility(View.GONE);
    }

    public void refresh() {
        request(url, true);
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    private void setup() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                return !WEBVIEW_LOGGING || super.onConsoleMessage(cm);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webViewInterface.showErrorToast(description);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                onReceivedError(view, err.getErrorCode(), err.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hideWebView();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webViewInterface.hideAnyProgress();
                webViewInterface.hideAnyMessage();
                showWebView();
                super.onPageFinished(view, url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.HOST) && webViewInterface.inAppLinksEnabled() || webViewInterface.externalLinksEnabled()) {
                    request(url, true);
                } else {
                    Uri uri = Uri.parse(url);
                    if (url.contains(Config.HOST) && UserManager.isAuthorized()) {
                        webViewInterface.openUrlInBrowser(uri, UserManager.getToken());
                    } else {
                        webViewInterface.openUrlInBrowser(uri, null);
                    }
                }
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((i == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });
    }

    public void request(String url, boolean userRequest) {
        if (Config.DEBUG) {
            Log.d(TAG, "Request URL: " + url);
        }
        if (url != null) {
            this.url = url;

            if (!webViewInterface.externalLinksEnabled() || Patterns.WEB_URL.matcher(this.url).matches()) {
                if (NetworkUtil.isOnline()) {
                    if (webViewIsShown()) {
                        webViewInterface.showProgressMessage();
                    } else {
                        webViewInterface.showRefreshProgress();
                    }
                    if (url.contains(Config.HOST)) {
                        Map<String, String> header = new HashMap<>();
                        header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE);
                        if (UserManager.isAuthorized()) {
                            header.put(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + UserManager.getToken());
                        }

                        // lanalytics context data cookie
                        String lanalyticsContextDataJson = LanalyticsUtil.getContextDataJson();
                        CookieManager.getInstance().setCookie(Config.HOST_URL, Config.LANALYTICS_CONTEXT_COOKIE + "=" + lanalyticsContextDataJson);

                        loadUrl(this.url, header);
                    } else {
                        loadUrl(this.url, null);
                    }
                } else {
                    webViewInterface.showNetworkRequiredMessage();
                    if (userRequest) {
                        webViewInterface.showNetworkRequiredToast();
                    }
                }
            } else {
                webViewInterface.showInvalidUrlToast();
            }
        }
    }

}
