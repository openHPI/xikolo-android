package de.xikolo.presenters.base;

public interface Presenter<V extends View> {

    void onViewAttached(V view);

    void onViewDetached();

    void onDestroyed();

}
