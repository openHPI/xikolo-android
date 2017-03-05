package de.xikolo.controllers.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import de.xikolo.R;
import de.xikolo.controllers.helper.LoadingStateController;
import de.xikolo.presenters.LoadingStatePresenter;
import de.xikolo.presenters.LoadingStateView;

public abstract class LoadingStatePresenterFragment<P extends LoadingStatePresenter<V>, V extends LoadingStateView> extends BasePresenterFragment<P, V> implements LoadingStateView, SwipeRefreshLayout.OnRefreshListener {

    protected LoadingStateController loadingStateController;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingStateController = new LoadingStateController(getActivity(), view, this);
        loadingStateController.hide();
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh();
    }

    @Override
    public void showProgressDialog() {
        loadingStateController.showBlockingProgress();
    }

    @Override
    public void hideProgressDialog() {
        loadingStateController.hide();
    }

    @Override
    public void showNetworkRequiredMessage() {

    }

    @Override
    public void showLoginRequiredMessage() {
        loadingStateController.setTitle(R.string.notification_please_login);
        loadingStateController.setSummary(R.string.notification_please_login_summary);
        loadingStateController.showMessage();
    }

    @Override
    public void showNoOfflineContentMessage() {

    }
}
