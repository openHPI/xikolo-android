package de.xikolo.controllers.helper;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.views.CustomFontTextView;

@SuppressWarnings("unused")
public class LoadingStateHelper {

    @BindView(R.id.containerEmptyMessage) FrameLayout viewMessage;
    @BindView(R.id.containerProgress) ProgressBar progressBar;
    @BindView(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;

    @BindView(R.id.textNotificationSymbol) CustomFontTextView textIcon;
    @BindView(R.id.textNotificationHeader) TextView textTitle;
    @BindView(R.id.textNotificationSummary) TextView textSummary;

    FragmentActivity activity;

    ProgressDialog progressDialog;

    public LoadingStateHelper(FragmentActivity activity, View view, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        this.activity = activity;

        ButterKnife.bind(this, view);

        if (viewMessage == null) {
            throw new RuntimeException("Layout does not contain Empty Message view");
        }
        if (progressBar == null) {
            throw new RuntimeException("Layout does not contain ProgressBar view");
        }
        if (refreshLayout == null) {
            throw new RuntimeException("Layout does not contain RefreshLayout");
        }

        RefeshLayoutHelper.setup(refreshLayout, onRefreshListener);
    }

    public void showProgress() {
        hide();
        progressBar.setVisibility(View.VISIBLE);
    }

    public void showBlockingProgress() {
        hide();
        progressDialog = ProgressDialog.getInstance();
        progressDialog.show(activity.getSupportFragmentManager(), ProgressDialog.TAG);
    }

    public void showRefreshProgress() {
        hide();
        refreshLayout.setRefreshing(true);
    }

    public void showMessage() {
        hide();
        viewMessage.setVisibility(View.VISIBLE);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        viewMessage.setOnClickListener(listener);
    }

    public CharSequence getSymbol() {
        return textIcon.getText();
    }

    public void setSymbol(String symbol) {
        textIcon.setText(symbol);
    }

    public void setSymbol(int title) {
        textIcon.setText(App.getInstance().getResources().getString(title));
    }

    public CharSequence getTitle() {
        return textTitle.getText();
    }

    public void setTitle(String title) {
        textTitle.setText(title);
    }

    public void setTitle(int title) {
        textTitle.setText(App.getInstance().getResources().getString(title));
    }

    public CharSequence getSummary() {
        return textSummary.getText();
    }

    public void setSummary(String summary) {
        textSummary.setText(summary);
    }

    public void setSummary(int summary) {
        textSummary.setText(App.getInstance().getResources().getString(summary));
    }

    public void hide() {
        progressBar.setVisibility(View.GONE);
        viewMessage.setVisibility(View.GONE);
        refreshLayout.setRefreshing(false);
        if (progressDialog != null && progressDialog.getDialog() != null && progressDialog.getDialog().isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void enableSwipeRefresh(boolean enabled) {
        refreshLayout.setEnabled(enabled);
    }

}
