package de.xikolo.controller.helper;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.view.CustomFontTextView;

public class NotificationController {

    Activity mActivity;

    private CardView cardView;
    private ProgressBar progressView;

    private CustomFontTextView symbolTextView;
    private TextView titleTextView;
    private TextView summaryTextView;

    private Boolean mIsLoading = false;
    private int mVisibilityNotification = FrameLayout.VISIBLE;

    public NotificationController(Activity activity, View layout) {
        this.mActivity = activity;

        cardView = (CardView) layout.findViewById(R.id.containerNotification);
        progressView = (ProgressBar) layout.findViewById(R.id.progress);

        if (cardView == null) {
            throw new IllegalArgumentException("Layout does not contain NotificationCard view");
        }
        if (progressView == null) {
            throw new IllegalArgumentException("Layout does not contain ProgressBar view");
        }

        symbolTextView = (CustomFontTextView) cardView.findViewById(R.id.textNotificationSymbol);
        titleTextView = (TextView) cardView.findViewById(R.id.textNotificationHeader);
        summaryTextView = (TextView) cardView.findViewById(R.id.textNotificationSummary);
    }

    public void setProgressVisible(Boolean isLoading) {
        if (!(this.mIsLoading == isLoading)) {
            this.mIsLoading = isLoading;

            if (mIsLoading) {
                progressView.setVisibility(ProgressBar.VISIBLE);
            } else {
                progressView.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    }

    public void setNotificationVisible(Boolean visible) {
        if (visible) {
            mVisibilityNotification = FrameLayout.VISIBLE;
        } else {
            mVisibilityNotification = FrameLayout.INVISIBLE;
        }

        cardView.setVisibility(mVisibilityNotification);
    }

    public Boolean isNotificationVisible() {
        if (mVisibilityNotification == FrameLayout.VISIBLE) {
            return true;
        }

        return false;
    }

    public Boolean isLoading() {
        return mIsLoading;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        cardView.setOnClickListener(listener);
    }

    public CharSequence getSymbol() {
        return symbolTextView.getText();
    }

    public void setSymbol(int title) {
        symbolTextView.setText(mActivity.getResources().getString(title));
    }

    public void setSymbol(String symbol) {
        symbolTextView.setText(symbol);
    }

    public CharSequence getTitle() {
        return titleTextView.getText();
    }

    public void setTitle(int title) {
        titleTextView.setText(mActivity.getResources().getString(title));
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public CharSequence getSummary() {
        return summaryTextView.getText();
    }

    public void setSummary(int summary) {
        summaryTextView.setText(mActivity.getResources().getString(summary));
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);
    }
}
