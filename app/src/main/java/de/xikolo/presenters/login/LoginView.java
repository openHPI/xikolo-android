package de.xikolo.presenters.login;

import de.xikolo.presenters.base.View;

public interface LoginView extends View {

    void showProgressDialog();

    void hideProgressDialog();

    void showLoginFailedToast();

    void showNoNetworkToast();

    void finishActivity();

    void showSSOView();

    void startSSOLogin(String strategy);

}
