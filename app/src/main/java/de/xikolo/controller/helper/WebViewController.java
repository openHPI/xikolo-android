package de.xikolo.controller.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class WebViewController implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewController.class.getSimpleName();

    private Activity mActivity;
    private WebView mWebView;
    private ProgressBar mProgress;

    private SwipeRefreshLayout mRefreshLayout;

    private String mUrl;

    private boolean mInAppLinksEnabled;

    public WebViewController(Activity activity, WebView webView, SwipeRefreshLayout refreshLayout, ProgressBar progress) {
        mActivity = activity;
        mWebView = webView;
        mRefreshLayout = refreshLayout;
        mProgress = progress;

        mInAppLinksEnabled = true;

        setup();

        RefeshLayoutController.setup(refreshLayout, this);
    }

    public void setInAppLinksEnabled(boolean enabled) {
        mInAppLinksEnabled = enabled;
    }

    private void setup() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.setWebChromeClient(new WebChromeClient());

        mProgress.setVisibility(View.VISIBLE);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ToastUtil.show(mActivity, "An error occurred" + description);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mRefreshLayout.setRefreshing(true);
                mWebView.setVisibility(View.GONE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgress.setVisibility(View.GONE);
                mRefreshLayout.setRefreshing(false);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.HOST) && mInAppLinksEnabled) {
                    request(url);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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

    public void request(String url) {
        if (Config.DEBUG) {
            Log.i(TAG, "Request URL: " + url);
        }
        mUrl = url;
        if (NetworkUtil.isOnline(mActivity)) {
            mRefreshLayout.setRefreshing(true);
            Map<String, String> header = new HashMap<String, String>();
            header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_VALUE_USER_PLATFORM_ANDROID);
            if (UserModel.isLoggedIn(mActivity)) {
                header.put(Config.HEADER_AUTHORIZATION, "Token " + UserModel.getToken(mActivity));
            }
            mWebView.loadUrl(mUrl, header);
        } else {
            mRefreshLayout.setRefreshing(false);
            NetworkUtil.showNoConnectionToast(mActivity);
        }
    }

    @Override
    public void onRefresh() {
        request(mUrl);
    }

}
