package de.xikolo.presenters.main;

import android.arch.lifecycle.LifecycleOwner;

import de.xikolo.presenters.base.PresenterFactory;

public class NavigationPresenterFactory implements PresenterFactory<NavigationPresenter> {

    private LifecycleOwner lifecycleOwner;

    public NavigationPresenterFactory(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public NavigationPresenter create() {
        return new NavigationPresenter(lifecycleOwner);
    }

}
