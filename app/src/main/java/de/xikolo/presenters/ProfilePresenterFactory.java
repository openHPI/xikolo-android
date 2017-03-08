package de.xikolo.presenters;

public class ProfilePresenterFactory implements PresenterFactory<ProfilePresenter> {

    @Override
    public ProfilePresenter create() {
        return new ProfilePresenter();
    }

}
