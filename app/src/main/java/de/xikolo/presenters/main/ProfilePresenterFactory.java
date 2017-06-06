package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class ProfilePresenterFactory implements PresenterFactory<ProfilePresenter> {

    @Override
    public ProfilePresenter create() {
        return new ProfilePresenter();
    }

}
