package de.xikolo.presenters.shared;

import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.managers.UserManager;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.config.Config;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

public class WebViewPresenter extends LoadingStatePresenter<PWebView> {

    public static final String TAG = WebViewPresenter.class.getSimpleName();

    protected String url;

    @Override
    public void onRefresh() {
        request(url, true);
    }

    public void setup(String url) {
        getViewOrThrow().showProgressMessage();
        request(url, false);
    }

    public void onReceivedError(String message) {
        getViewOrThrow().showErrorToast(message);
    }

    public void onPageStarted() {
        getViewOrThrow().hideWebView();
    }

    public void onPageFinished() {
        getViewOrThrow().hideAnyProgress();
        getViewOrThrow().hideAnyMessage();
        getViewOrThrow().showWebView();
    }

    public void onUrlLoading(String url) {
        if (url.contains(Config.HOST) && getViewOrThrow().inAppLinksEnabled() || getViewOrThrow().externalLinksEnabled()) {
            request(url, true);
        } else {
            Uri uri = Uri.parse(url);
            if (url.contains(Config.HOST) && UserManager.isAuthorized()) {
                getViewOrThrow().openUrlInBrowser(uri, UserManager.getToken());
            } else {
                getViewOrThrow().openUrlInBrowser(uri, null);
            }
        }
    }

    private void request(String url, boolean userRequest) {
        if (Config.DEBUG) {
            Log.i(TAG, "Request URL: " + url);
        }
        if (url != null) {
            this.url = url;

            if (!getViewOrThrow().externalLinksEnabled() || Patterns.WEB_URL.matcher(this.url).matches()) {
                if (NetworkUtil.isOnline()) {
                    if (getViewOrThrow().webViewIsShown()) {
                        getViewOrThrow().showProgressMessage();
                    } else {
                        getViewOrThrow().showRefreshProgress();
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

                        getViewOrThrow().loadUrl(this.url, header);
                    } else {
                        getViewOrThrow().loadUrl(this.url, null);
                    }
                } else {
                    getViewOrThrow().showNetworkRequiredMessage();
                    if (userRequest) {
                        getViewOrThrow().showNetworkRequiredToast();
                    }
                }
            } else {
                getViewOrThrow().showInvalidUrlToast();
            }
        }
    }

}
