package de.xikolo.controllers.base;

import android.support.annotation.LayoutRes;

public interface LoadingStateInterface {

    void showContent();

    void showBlockingProgress();

    void showProgress();

    void hideProgress();

    void showNetworkRequiredMessage();

    void showLoginRequiredMessage();

    void hideMessage();

    void showErrorMessage();

    @LayoutRes int getLayoutResource();

}
