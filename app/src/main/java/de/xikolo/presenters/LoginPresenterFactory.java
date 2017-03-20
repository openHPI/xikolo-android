package de.xikolo.presenters;

public class LoginPresenterFactory implements PresenterFactory<LoginPresenter> {

    @Override
    public LoginPresenter create() {
        return new LoginPresenter();
    }

}
