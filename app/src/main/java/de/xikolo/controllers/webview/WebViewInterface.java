package de.xikolo.controllers.webview;

import android.net.Uri;

import de.xikolo.controllers.base.LoadingStateInterface;

public interface WebViewInterface extends LoadingStateInterface {

    void showInvalidUrlToast();

    void showErrorToast(String message);

    boolean inAppLinksEnabled();

    boolean externalLinksEnabled();

    void openUrlInBrowser(Uri uri, String token);

}
