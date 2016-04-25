package de.xikolo.controller.helper;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.view.CustomFontTextView;

@SuppressWarnings("unused")
public class NotificationController {

    private FrameLayout viewNotification;
    private ProgressBar progressBar;

    private CustomFontTextView textIcon;
    private TextView textTitle;
    private TextView textSummary;

    public NotificationController(View layout) {
        viewNotification = (FrameLayout) layout.findViewById(R.id.containerNotification);
        progressBar = (ProgressBar) layout.findViewById(R.id.containerProgress);

        if (viewNotification == null) {
            throw new IllegalArgumentException("Layout does not contain Notification view");
        }
        if (progressBar == null) {
            throw new IllegalArgumentException("Layout does not contain ProgressBar view");
        }

        textIcon = (CustomFontTextView) viewNotification.findViewById(R.id.textNotificationSymbol);
        textTitle = (TextView) viewNotification.findViewById(R.id.textNotificationHeader);
        textSummary = (TextView) viewNotification.findViewById(R.id.textNotificationSummary);
    }

    public void setProgressVisible(Boolean visible) {
        if (visible) {
            progressBar.setVisibility(View.VISIBLE);
            setNotificationVisible(false);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setNotificationVisible(Boolean visible) {
        if (visible) {
            viewNotification.setVisibility(View.VISIBLE);
            setProgressVisible(false);
        } else {
            viewNotification.setVisibility(View.GONE);
        }
    }

    public Boolean isNotificationVisible() {
        return viewNotification.getVisibility() == View.VISIBLE;
    }

    public Boolean isProgressVisible() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        viewNotification.setOnClickListener(listener);
    }

    public CharSequence getSymbol() {
        return textIcon.getText();
    }

    public void setSymbol(String symbol) {
        textIcon.setText(symbol);
    }

    public void setSymbol(int title) {
        textIcon.setText(GlobalApplication.getInstance().getResources().getString(title));
    }

    public CharSequence getTitle() {
        return textTitle.getText();
    }

    public void setTitle(String title) {
        textTitle.setText(title);
    }

    public void setTitle(int title) {
        textTitle.setText(GlobalApplication.getInstance().getResources().getString(title));
    }

    public CharSequence getSummary() {
        return textSummary.getText();
    }

    public void setSummary(String summary) {
        textSummary.setText(summary);
    }

    public void setSummary(int summary) {
        textSummary.setText(GlobalApplication.getInstance().getResources().getString(summary));
    }

    public void setInvisible() {
        setNotificationVisible(false);
        setProgressVisible(false);
    }

}
