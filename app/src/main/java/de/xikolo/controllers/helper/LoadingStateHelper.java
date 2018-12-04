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
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate;
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle;
import de.xikolo.views.CustomFontTextView;

@SuppressWarnings("unused")
public class LoadingStateHelper {

    @BindView(R.id.content_view) View contentView;

    @BindView(R.id.container_content_message) FrameLayout messageContainer;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;

    @BindView(R.id.text_notification_symbol) CustomFontTextView textIcon;
    @BindView(R.id.text_notification_header) TextView textHeader;
    @BindView(R.id.text_notification_summary) TextView textSummary;

    private FragmentActivity activity;

    private ProgressDialogIndeterminate progressDialog;

    public LoadingStateHelper(FragmentActivity activity, View view, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        this.activity = activity;

        ButterKnife.bind(this, view);

        RefeshLayoutHelper.setup(refreshLayout, onRefreshListener);

        contentView.setVisibility(View.GONE);
        hideMessage();
        hideProgress();
    }

    public void showContentView() {
        hideMessage();
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            refreshLayout.setRefreshing(true);
        }
        contentView.setVisibility(View.VISIBLE);
    }

    public void hideContentView() {
        contentView.setVisibility(View.GONE);
    }

    public boolean isContentViewVisible() {
        return contentView.getVisibility() == View.VISIBLE;
    }

    public void showProgress() {
        if (isContentViewVisible() || isMessageVisible()) {
            if (!refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(true);
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void showBlockingProgress() {
        progressDialog = ProgressDialogIndeterminateAutoBundle.builder().build();
        progressDialog.show(activity.getSupportFragmentManager(), ProgressDialogIndeterminate.TAG);
    }

    public void hideProgress() {
        refreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        if (progressDialog != null && progressDialog.getDialog() != null && progressDialog.getDialog().isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showMessage() {
        hideProgress();
        messageContainer.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {
        messageContainer.setVisibility(View.GONE);
    }

    private boolean isMessageVisible() {
        return messageContainer.getVisibility() == View.VISIBLE;
    }

    public void setMessageOnClickListener(View.OnClickListener listener) {
        messageContainer.setOnClickListener(listener);
    }

    public CharSequence getMessageSymbol() {
        return textIcon.getText();
    }

    public void setMessageSymbol(String symbol) {
        textIcon.setText(symbol);
    }

    public void setMessageSymbol(int title) {
        textIcon.setText(App.getInstance().getResources().getString(title));
    }

    public CharSequence getMessageTitle() {
        return textHeader.getText();
    }

    public void setMessageTitle(String title) {
        textHeader.setText(title);
    }

    public void setMessageTitle(int title) {
        textHeader.setText(App.getInstance().getResources().getString(title));
    }

    public CharSequence getMessageSummary() {
        return textSummary.getText();
    }

    public void setMessageSummary(String summary) {
        if (summary == null) {
            textSummary.setVisibility(View.GONE);
        } else {
            textSummary.setVisibility(View.VISIBLE);
        }
        textSummary.setText(summary);
    }

    public void setMessageSummary(int summary) {
        if (summary == 0) {
            textSummary.setText(null);
            textSummary.setVisibility(View.GONE);
        } else {
            textSummary.setText(App.getInstance().getResources().getString(summary));
            textSummary.setVisibility(View.VISIBLE);
        }
    }

    public void enableSwipeRefresh(boolean enabled) {
        refreshLayout.setEnabled(enabled);
    }

}
