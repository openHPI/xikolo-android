package de.xikolo.presenters;

public interface Presenter<V> {

    void onViewAttached(V view);

    void onViewDetached();

    void onDestroyed();

}
