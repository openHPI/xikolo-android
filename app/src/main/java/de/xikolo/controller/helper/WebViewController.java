package de.xikolo.controller.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class WebViewController implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewController.class.getSimpleName();

    private Context context;
    private WebView webView;
    private NotificationController notificationController;

    private SwipeRefreshLayout refreshLayout;

    private String url;

    private boolean inAppLinksEnabled;
    private boolean loadExternalUrlEnabled;

    public WebViewController(Context context, View layout) {
        this.context = context;
        webView = (WebView) layout.findViewById(R.id.webView);
        refreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        notificationController = new NotificationController(layout);

        inAppLinksEnabled = true;
        loadExternalUrlEnabled = false;

        setup();

        RefeshLayoutController.setup(refreshLayout, this);
    }

    public void setInAppLinksEnabled(boolean enabled) {
        inAppLinksEnabled = enabled;
    }

    public void setLoadExternalUrlEnabled(boolean loadExt) {
        this.loadExternalUrlEnabled = loadExt;
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    private void setup() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient());

        notificationController.setProgressVisible(true);

        webView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ToastUtil.show("An error occurred" + description);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                onReceivedError(view, err.getErrorCode(), err.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!notificationController.isProgressVisible()) {
                    refreshLayout.setRefreshing(true);
                }
                webView.setVisibility(View.GONE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                notificationController.setInvisible();
                refreshLayout.setRefreshing(false);
                webView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.HOST) && inAppLinksEnabled || loadExternalUrlEnabled) {
                    request(url, true);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (url.contains(Config.HOST) && UserModel.isLoggedIn(context)) {
                        Bundle headers = new Bundle();
                        headers.putString(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + UserModel.getToken(context));
                        i.putExtra(Browser.EXTRA_HEADERS, headers);
                    }
                    context.startActivity(i);
                }
                return true;
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
            Log.i(TAG, "Request URL: " + url);
        }
        if (url != null) {
            this.url = url;

            if (!loadExternalUrlEnabled || Patterns.WEB_URL.matcher(this.url).matches()) {
                if (NetworkUtil.isOnline(context)) {
                    if (!notificationController.isProgressVisible()) {
                        refreshLayout.setRefreshing(true);
                    }
                    if (url.contains(Config.HOST)) {
                        Map<String, String> header = new HashMap<>();
                        header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE);
                        if (UserModel.isLoggedIn(context)) {
                            header.put(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + UserModel.getToken(context));
                        }

                        CookieManager.getInstance().setCookie(Config.URI, Config.COOKIE_LANALYTICS_CONTEXT + "=" + GlobalApplication.getInstance()
                                .getLanalytics().getDefaultContextPayload() + "; Domain=" + Config.URI);

                        webView.loadUrl(this.url, header);
                    } else {
                        webView.loadUrl(this.url, null);
                    }
                } else {
                    refreshLayout.setRefreshing(false);

                    notificationController.setTitle(R.string.notification_no_network);
                    notificationController.setSummary(R.string.notification_no_network_summary);
                    notificationController.setNotificationVisible(true);

                    if (userRequest) {
                        NetworkUtil.showNoConnectionToast();
                    }
                }
            } else {
                notificationController.setTitle(R.string.notification_url_invalid);
            }
        }
    }

    @Override
    public void onRefresh() {
        request(url, true);
    }

}
