package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class NavigationPresenterFactory implements PresenterFactory<NavigationPresenter> {

    @Override
    public NavigationPresenter create() {
        return new NavigationPresenter();
    }

}
