package de.xikolo.presenters;

public interface LoginView {

    void showProgressDialog();

    void hideProgressDialog();

    void showLoginFailedToast();

    void showNoNetworkToast();

    void finishActivity();

}
