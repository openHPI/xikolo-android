package de.xikolo.controller.helper;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.view.CustomFontTextView;

public class NotificationController {

    Activity mActivity;

    private CardView cardView;
    private View layout_notification_text;
    private View layout_notification_loading;

    private CustomFontTextView symbolTextView;
    private TextView titleTextView;
    private TextView summaryTextView;

    private Boolean mIsLoading = false;
    private int mVisibility = FrameLayout.VISIBLE;

    public NotificationController(Activity activity, View layout) {
        this.mActivity = activity;

        LayoutInflater inflater = mActivity.getLayoutInflater();

        cardView = (CardView) layout.findViewById(R.id.containerNotification);
        layout_notification_text = inflater.inflate(R.layout.container_notification_text, null);
        layout_notification_loading = inflater.inflate(R.layout.container_progress, null);

        if (cardView == null) {
            throw new IllegalArgumentException("Layout does not contain NotificationCard view");
        }

        symbolTextView = (CustomFontTextView) layout_notification_text.findViewById(R.id.textNotificationSymbol);
        titleTextView = (TextView) layout_notification_text.findViewById(R.id.textNotificationHeader);
        summaryTextView = (TextView) layout_notification_text.findViewById(R.id.textNotificationSummary);

        cardView.addView(layout_notification_text);
    }

    public void setLoading(Boolean isLoading) {
        if (!(this.mIsLoading == isLoading)) {
            this.mIsLoading = isLoading;

            cardView.removeAllViews();

            if (mIsLoading) {
                cardView.addView(layout_notification_loading);
            } else {
                cardView.addView(layout_notification_text);
            }
        }
    }

    public void setVisible(Boolean visible) {
        if (visible) {
            mVisibility = FrameLayout.VISIBLE;
        } else {
            mVisibility = FrameLayout.INVISIBLE;
        }

        cardView.setVisibility(mVisibility);
    }

    public Boolean isVisible() {
        if (mVisibility == FrameLayout.VISIBLE) {
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
