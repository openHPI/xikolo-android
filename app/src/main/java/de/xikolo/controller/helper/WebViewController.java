package de.xikolo.controller.helper;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.R;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class WebViewController implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewController.class.getSimpleName();

    private Activity mActivity;
    private WebView mWebView;
    private NotificationController mNotificationController;

    private SwipeRefreshLayout mRefreshLayout;

    private String mUrl;

    private boolean mInAppLinksEnabled;
    private boolean mLoadExternalUrlEnabled;

    public WebViewController(Activity activity, View layout) {
        mActivity = activity;
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        mNotificationController = new NotificationController(layout);

        mInAppLinksEnabled = true;
        mLoadExternalUrlEnabled = false;

        setup();

        RefeshLayoutController.setup(mRefreshLayout, this);
    }

    public void setInAppLinksEnabled(boolean enabled) {
        mInAppLinksEnabled = enabled;
    }

    public void setLoadExternalUrlEnabled(boolean loadExt) {
        this.mLoadExternalUrlEnabled = loadExt;
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    private void setup() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setWebChromeClient(new WebChromeClient());

        mNotificationController.setProgressVisible(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ToastUtil.show(mActivity, "An error occurred" + description);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                onReceivedError(view, err.getErrorCode(), err.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!mNotificationController.isProgressVisible()) {
                    mRefreshLayout.setRefreshing(true);
                }
                mWebView.setVisibility(View.GONE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mNotificationController.setInvisible();
                mRefreshLayout.setRefreshing(false);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.HOST) && mInAppLinksEnabled || mLoadExternalUrlEnabled) {
                    request(url, true);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (url.contains(Config.HOST) && UserModel.isLoggedIn(mActivity)) {
                        Bundle headers = new Bundle();
                        headers.putString(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_VALUE_SCHEMA + UserModel.getToken(mActivity));
                        i.putExtra(Browser.EXTRA_HEADERS, headers);
                    }
                    mActivity.startActivity(i);
                }
                return true;
            }

        });

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((i == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                    mWebView.goBack();
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
            mUrl = url;

            if (!mLoadExternalUrlEnabled || Patterns.WEB_URL.matcher(mUrl).matches()) {
                if (NetworkUtil.isOnline(mActivity)) {
                    if (!mNotificationController.isProgressVisible()) {
                        mRefreshLayout.setRefreshing(true);
                    }
                    if (url.contains(Config.HOST)) {
                        Map<String, String> header = new HashMap<>();
                        header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE);
                        if (UserModel.isLoggedIn(mActivity)) {
                            header.put(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_VALUE_SCHEMA + UserModel.getToken(mActivity));
                        }
                        mWebView.loadUrl(mUrl, header);
                    } else {
                        mWebView.loadUrl(mUrl, null);
                    }
                } else {
                    mRefreshLayout.setRefreshing(false);

                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_summary);
                    mNotificationController.setNotificationVisible(true);

                    if (userRequest) {
                        NetworkUtil.showNoConnectionToast(mActivity);
                    }
                }
            } else {
                mNotificationController.setTitle(R.string.notification_url_invalid);
            }
        }
    }

    @Override
    public void onRefresh() {
        request(mUrl, true);
    }

    public void saveState(Bundle outState) {
        if (mWebView != null) {
            mWebView.saveState(outState);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.restoreState(savedInstanceState);
            mUrl = mWebView.getUrl();
        }
    }

}
