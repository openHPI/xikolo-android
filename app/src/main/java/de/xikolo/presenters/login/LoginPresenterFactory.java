package de.xikolo.presenters.login;

import de.xikolo.presenters.base.PresenterFactory;

public class LoginPresenterFactory implements PresenterFactory<LoginPresenter> {

    @Override
    public LoginPresenter create() {
        return new LoginPresenter();
    }

}
