package de.xikolo.controllers.base;

public interface LoadingStateInterface {

    void showProgressDialog();

    void showProgressMessage();

    void showRefreshProgress();

    void hideAnyProgress();

    void showNetworkRequiredMessage();

    void showNetworkRequiredToast();

    void showLoginRequiredMessage();

    void showLoginRequiredToast();

    void hideAnyMessage();

    void showErrorToast();

    void enableSwipeRefresh(boolean enabled);

}
