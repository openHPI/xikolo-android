package de.xikolo.presenters.base;

public abstract class LoadingStatePresenter<V extends LoadingStateView> extends Presenter<V> {

    public abstract void onRefresh();

}
