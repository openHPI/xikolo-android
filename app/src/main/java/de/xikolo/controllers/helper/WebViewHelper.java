package de.xikolo.controllers.helper;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
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
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.controllers.webview.WebViewFragment;
import de.xikolo.managers.UserManager;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static de.xikolo.config.Config.WEBVIEW_LOGGING;

public class WebViewHelper {

    public static final String TAG = WebViewHelper.class.getSimpleName();

    @BindView(R.id.content_view) WebView webView;

    protected String url;

    private WebViewFragment webViewInterface;

    public WebViewHelper(View view, WebViewFragment callback) {
        ButterKnife.bind(this, view);
        webViewInterface = callback;

        setup();
    }

    private void loadUrl(String url, Map<String, String> header) {
        webView.loadUrl(url, header);
    }

    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    private void hideWebView() {
        webView.setVisibility(View.GONE);
    }

    public void refresh() {
        request(url);
    }

    public String requestedUrl() {
        return url;
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    private void setup() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setUserAgentString(Config.HEADER_USER_AGENT_VALUE);

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
                if (Config.DEBUG) Log.e(TAG, "An error occurred: " + description);
            }

            @TargetApi(M)
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
                webViewInterface.hideMessage();
                showWebView();
                App.getInstance().getState().getConnectivity().online();
                super.onPageFinished(view, url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.HOST) && url.contains("/auth/app")) {
                    String token = Uri.parse(url).getQueryParameter("token");
                    webViewInterface.interceptSSOLogin(token);
                } else if (url.contains(Config.HOST) && webViewInterface.inAppLinksEnabled() || webViewInterface.externalLinksEnabled()) {
                    request(url);
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

            @TargetApi(N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

        });

        webView.setOnKeyListener((view, keyCode, keyEvent) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
            return false;
        });
    }

    public void request(String url) {
        if (Config.DEBUG) {
            Log.d(TAG, "Request URL: " + url);
        }
        if (url != null) {
            this.url = url;

            if (!webViewInterface.externalLinksEnabled() || Patterns.WEB_URL.matcher(this.url).matches()) {
                if (NetworkUtil.isOnline()) {
                    webViewInterface.showAnyProgress();
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
                    webViewInterface.hideAnyProgress();
                    webViewInterface.showNetworkRequired();
                    App.getInstance().getState().getConnectivity().offline();
                }
            } else {
                webViewInterface.hideAnyProgress();
                webViewInterface.showInvalidUrlToast();
            }
        } else {
            webViewInterface.hideAnyProgress();
        }
    }

}
