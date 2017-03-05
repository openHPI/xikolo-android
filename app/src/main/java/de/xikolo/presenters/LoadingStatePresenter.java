package de.xikolo.presenters;

public interface LoadingStatePresenter<V extends LoadingStateView> extends Presenter<V> {

    void onRefresh();

}
