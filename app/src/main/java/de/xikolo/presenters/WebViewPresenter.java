package de.xikolo.presenters;

import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.managers.UserManager;
import de.xikolo.utils.Config;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

public class WebViewPresenter implements LoadingStatePresenter<PWebView> {

    public static final String TAG = WebViewPresenter.class.getSimpleName();

    protected PWebView view;

    protected String url;

    @Override
    public void onViewAttached(PWebView view) {
        this.view = view;
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }

    @Override
    public void onDestroyed() {
    }

    @Override
    public void onRefresh() {
        request(url, true);
    }

    public void setup(String url) {
        view.showProgressMessage();
        request(url, false);
    }

    public void request(String url, boolean userRequest) {
        if (Config.DEBUG) {
            Log.i(TAG, "Request URL: " + url);
        }
        if (url != null) {
            this.url = url;

            if (!view.externalLinksEnabled() || Patterns.WEB_URL.matcher(this.url).matches()) {
                if (NetworkUtil.isOnline()) {
                    if (view.webViewIsShown()) {
                        view.showProgressMessage();
                    } else {
                        view.showRefreshProgress();
                    }
                    if (url.contains(Config.HOST)) {
                        Map<String, String> header = new HashMap<>();
                        header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE);
                        if (UserManager.isAuthorized()) {
                            header.put(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + UserManager.getToken());
                        }

                        // lanalytics context data cookie
                        String lanalyticsContextDataJson = LanalyticsUtil.getContextDataJson();
                        CookieManager.getInstance().setCookie(Config.URI, Config.COOKIE_LANALYTICS_CONTEXT + "=" + lanalyticsContextDataJson);

                        view.loadUrl(this.url, header);
                    } else {
                        view.loadUrl(this.url, null);
                    }
                } else {
                    view.showNetworkRequiredMessage();
                    if (userRequest) {
                        view.showNetworkRequiredToast();
                    }
                }
            } else {
                view.showInvalidUrlToast();
            }
        }
    }

    public void onReceivedError(String message) {
        view.showErrorToast(message);
    }

    public void onPageStarted() {
        view.hideWebView();
    }

    public void onPageFinished() {
        view.hideAnyProgress();
        view.hideAnyMessage();
        view.showWebView();
    }

    public void onUrlLoading(String url) {
        if (url.contains(Config.HOST) && view.inAppLinksEnabled() || view.externalLinksEnabled()) {
            request(url, true);
        } else {
            Uri uri = Uri.parse(url);
            if (url.contains(Config.HOST) && UserManager.isAuthorized()) {
                view.openUrlInBrowser(uri, UserManager.getToken());
            } else {
                view.openUrlInBrowser(uri, null);
            }
        }
    }

}
