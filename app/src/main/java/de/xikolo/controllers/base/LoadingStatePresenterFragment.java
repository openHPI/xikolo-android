package de.xikolo.controllers.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import de.xikolo.R;
import de.xikolo.controllers.helper.LoadingStateController;
import de.xikolo.presenters.LoadingStatePresenter;
import de.xikolo.presenters.LoadingStateView;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

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
    public void showProgressMessage() {
        loadingStateController.showProgress();
    }

    @Override
    public void showRefreshProgress() {
        loadingStateController.showRefreshProgress();
    }

    @Override
    public void hideAnyProgress() {
        loadingStateController.hide();
    }

    @Override
    public void showNetworkRequiredMessage() {
        loadingStateController.setTitle(R.string.notification_no_network);
        loadingStateController.setSummary(R.string.notification_no_network_summary);
        loadingStateController.showMessage();
    }

    @Override
    public void showNetworkRequiredToast() {
        NetworkUtil.showNoConnectionToast();
    }

    @Override
    public void showLoginRequiredMessage() {
        loadingStateController.setTitle(R.string.notification_please_login);
        loadingStateController.setSummary(R.string.notification_please_login_summary);
        loadingStateController.showMessage();
    }

    @Override
    public void showLoginRequiredToast() {
        ToastUtil.show(R.string.toast_please_log_in);
    }

    @Override
    public void hideAnyMessage() {
        loadingStateController.hide();
    }

    @Override
    public void showErrorToast() {
        ToastUtil.show(R.string.error);
    }

}
