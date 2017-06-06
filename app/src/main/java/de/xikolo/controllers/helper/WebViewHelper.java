package de.xikolo.controllers.helper;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.presenters.shared.WebViewPresenter;

public class WebViewHelper {

    public static final String TAG = WebViewHelper.class.getSimpleName();

    @BindView(R.id.webView) private WebView webView;

    public WebViewHelper(View view) {
        ButterKnife.bind(this, view);
    }

    public void loadUrl(String url, Map<String, String> header) {
        webView.loadUrl(url, header);
    }

    public boolean webViewIsShown() {
        return webView.getVisibility() == View.VISIBLE;
    }

    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    public void hideWebView() {
        webView.setVisibility(View.GONE);
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    public void setup(final WebViewPresenter presenter, String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                presenter.onReceivedError(description);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                onReceivedError(view, err.getErrorCode(), err.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                presenter.onPageStarted();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                presenter.onPageFinished();
                super.onPageFinished(view, url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                presenter.onUrlLoading(url);
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

        presenter.setup(url);
    }

}
