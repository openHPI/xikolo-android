package de.xikolo.controllers.base;

import androidx.annotation.LayoutRes;

public interface LoadingStateInterface {

    void showContent();

    void hideContent();

    void showBlockingProgress();

    void showProgress();

    void hideProgress();

    void showNetworkRequiredMessage();

    void showLoginRequiredMessage();

    void hideMessage();

    void showErrorMessage();

    boolean isContentViewVisible();

    @LayoutRes int getLayoutResource();

}
