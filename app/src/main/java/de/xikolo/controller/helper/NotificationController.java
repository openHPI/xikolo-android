package de.xikolo.controller.helper;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.view.CustomFontTextView;

public class NotificationController {

    private CardView cardView;
    private ProgressBar progressView;

    private CustomFontTextView symbolTextView;
    private TextView titleTextView;
    private TextView summaryTextView;

    public NotificationController(View layout) {
        cardView = (CardView) layout.findViewById(R.id.containerNotification);
        progressView = (ProgressBar) layout.findViewById(R.id.containerProgress);

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

    public void setProgressVisible(Boolean visible) {
        if (visible) {
            progressView.setVisibility(View.VISIBLE);
            setNotificationVisible(false);
        } else {
            progressView.setVisibility(View.GONE);
        }
    }

    public void setNotificationVisible(Boolean visible) {
        if (visible) {
            cardView.setVisibility(View.VISIBLE);
            setProgressVisible(false);
        } else {
            cardView.setVisibility(View.GONE);
        }
    }

    public Boolean isNotificationVisible() {
        return cardView.getVisibility() == View.VISIBLE;
    }

    public Boolean isProgressVisible() {
        return progressView.getVisibility() == View.VISIBLE;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        cardView.setOnClickListener(listener);
    }

    public CharSequence getSymbol() {
        return symbolTextView.getText();
    }

    public void setSymbol(String symbol) {
        symbolTextView.setText(symbol);
    }

    public void setSymbol(int title) {
        symbolTextView.setText(GlobalApplication.getInstance().getResources().getString(title));
    }

    public CharSequence getTitle() {
        return titleTextView.getText();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setTitle(int title) {
        titleTextView.setText(GlobalApplication.getInstance().getResources().getString(title));
    }

    public CharSequence getSummary() {
        return summaryTextView.getText();
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);
    }

    public void setSummary(int summary) {
        summaryTextView.setText(GlobalApplication.getInstance().getResources().getString(summary));
    }

    public void setInvisible() {
        setNotificationVisible(false);
        setProgressVisible(false);
    }

}
