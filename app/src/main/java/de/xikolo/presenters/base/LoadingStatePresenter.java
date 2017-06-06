package de.xikolo.presenters.base;

public interface LoadingStatePresenter<V extends LoadingStateView> extends Presenter<V> {

    void onRefresh();

}
