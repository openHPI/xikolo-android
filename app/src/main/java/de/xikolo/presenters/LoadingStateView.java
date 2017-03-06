package de.xikolo.presenters;

public interface LoadingStateView {

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
 
}
