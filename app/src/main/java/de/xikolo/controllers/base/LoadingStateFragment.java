package de.xikolo.controllers.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import de.xikolo.R;
import de.xikolo.controllers.helper.LoadingStateHelper;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

public abstract class LoadingStateFragment extends BaseFragment implements LoadingStateInterface, SwipeRefreshLayout.OnRefreshListener {

    protected LoadingStateHelper loadingStateHelper;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingStateHelper = new LoadingStateHelper(getActivity(), view, this);
        loadingStateHelper.hide();
    }

    @Override
    public void showProgressDialog() {
        loadingStateHelper.showBlockingProgress();
    }

    @Override
    public void showProgressMessage() {
        loadingStateHelper.showProgress();
    }

    @Override
    public void showRefreshProgress() {
        loadingStateHelper.showRefreshProgress();
    }

    @Override
    public void hideAnyProgress() {
        loadingStateHelper.hide();
    }

    @Override
    public void showNetworkRequiredMessage() {
        loadingStateHelper.setTitle(R.string.notification_no_network);
        loadingStateHelper.setSummary(R.string.notification_no_network_summary);
        loadingStateHelper.showMessage();
    }

    @Override
    public void showNetworkRequiredToast() {
        NetworkUtil.showNoConnectionToast();
    }

    @Override
    public void showLoginRequiredMessage() {
        loadingStateHelper.setTitle(R.string.notification_please_login);
        loadingStateHelper.setSummary(R.string.notification_please_login_summary);
        loadingStateHelper.showMessage();
    }

    @Override
    public void showLoginRequiredToast() {
        ToastUtil.show(R.string.toast_please_log_in);
    }

    @Override
    public void hideAnyMessage() {
        loadingStateHelper.hide();
    }

    @Override
    public void showErrorToast() {
        ToastUtil.show(R.string.error);
    }

    @Override
    public void enableSwipeRefresh(boolean enabled) {
        loadingStateHelper.enableSwipeRefresh(enabled);
    }

}
