package de.xikolo.controllers.base;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.xikolo.R;
import de.xikolo.controllers.helper.LoadingStateHelper;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.presenters.base.LoadingStateView;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

public abstract class LoadingStatePresenterFragment<P extends LoadingStatePresenter<V>, V extends LoadingStateView> extends BasePresenterFragment<P, V> implements LoadingStateView, SwipeRefreshLayout.OnRefreshListener {

    protected LoadingStateHelper loadingStateHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate generic loading state view
        ViewGroup loadingStateView = (ViewGroup) inflater.inflate(R.layout.fragment_loading_state, container, false);
        // inflate content view inside
        ViewStub contentView = loadingStateView.findViewById(R.id.content_view);
        contentView.setLayoutResource(getLayoutResource());
        contentView.inflate();
        // return complete view
        return loadingStateView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingStateHelper = new LoadingStateHelper(getActivity(), view, this);
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh();
    }

    @Override
    public void hideContent() {
        loadingStateHelper.hideContentView();
    }

    @Override
    public void showContent() {
        loadingStateHelper.showContentView();
    }

    @Override
    public void showBlockingProgress() {
        loadingStateHelper.showBlockingProgress();
    }

    @Override
    public void showProgress() {
        loadingStateHelper.showProgress();
    }

    @Override
    public void hideProgress() {
        loadingStateHelper.hideProgress();
    }

    @Override
    public void showNetworkRequiredMessage() {
        if (loadingStateHelper.isContentViewVisible()) {
            NetworkUtil.showNoConnectionToast();
        } else {
            loadingStateHelper.setMessageTitle(R.string.notification_no_network);
            loadingStateHelper.setMessageSummary(R.string.notification_no_network_summary);
            loadingStateHelper.showMessage();
        }
    }

    @Override
    public void showLoginRequiredMessage() {
        if (loadingStateHelper.isContentViewVisible()) {
            ToastUtil.show(R.string.toast_please_log_in);
        } else {
            loadingStateHelper.setMessageTitle(R.string.notification_please_login);
            loadingStateHelper.setMessageSummary(R.string.notification_please_login_summary);
            loadingStateHelper.showMessage();
        }
    }

    @Override
    public void showErrorMessage() {
        if (loadingStateHelper.isContentViewVisible()) {
            ToastUtil.show(R.string.error);
        } else {
            loadingStateHelper.setMessageTitle(R.string.error);
            loadingStateHelper.setMessageSummary(null);
            loadingStateHelper.showMessage();
        }
    }

    @Override
    public void hideMessage() {
        loadingStateHelper.hideMessage();
    }

    @Override
    public boolean isContentViewVisible() {
        return loadingStateHelper.isContentViewVisible();
    }
}
