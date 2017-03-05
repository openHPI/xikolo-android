package de.xikolo.presenters;

public interface LoadingStateView {

    void showProgressDialog();

    void hideProgressDialog();

    void showNetworkRequiredMessage();

    void showLoginRequiredMessage();

    void showNoOfflineContentMessage();

}
