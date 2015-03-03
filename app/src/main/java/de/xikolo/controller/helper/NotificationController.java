package de.xikolo.controller.helper;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.controller.MainActivity;
import de.xikolo.view.CustomFontTextView;

public class NotificationController {

    Activity activity;

    private CardView cardView;
    private View layout_notification_text;
    private View layout_notification_loading;

    private CustomFontTextView symbolTextView;
    private TextView titleTextView;
    private TextView summaryTextView;
    private LinearLayout linearLayoutNotificationText;
    private LinearLayout.LayoutParams linearLayoutNotificationTextParams;
    private LinearLayout linearLayoutCard;

    private Boolean mIsLoading = false;

    public NotificationController(Activity activity, View layout) {
        LayoutInflater inflater = activity.getLayoutInflater();

        this.activity = activity;

        cardView = (CardView) layout.findViewById(R.id.containerNotification);
        layout_notification_text = inflater.inflate(R.layout.layout_notification_text, null);
        layout_notification_loading = inflater.inflate(R.layout.layout_notification_loading, null);

        if(cardView == null) {
            // TODO Exceptions werfen
        }

        symbolTextView = (CustomFontTextView) layout_notification_text.findViewById(R.id.textNotificationSymbol);
        titleTextView = (TextView) layout_notification_text.findViewById(R.id.textNotificationHeader);
        summaryTextView = (TextView) layout_notification_text.findViewById(R.id.textNotificationSummary);

        cardView.addView(layout_notification_text);
    }

    public void setLoading(Boolean isLoading) {
        this.mIsLoading = isLoading;

        cardView.removeAllViews();

        if(mIsLoading) {
            cardView.addView(layout_notification_loading);
        } else {
            cardView.addView(layout_notification_text);
        }
    }

    public void setVisible(Boolean visible) {
        int visibility = 0;
        if(visible) {
            visibility = FrameLayout.VISIBLE;
        } else {
            visibility = FrameLayout.INVISIBLE;
        }
        cardView.setVisibility(visibility);
    }

    public Boolean isVisible() {
        int visibility = cardView.getVisibility();

        if(visibility == FrameLayout.VISIBLE) {
            return true;
        }

        return  false;
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

    public void setSymbol(String symbol) {
        symbolTextView.setText(symbol);
    }
    public void setSymbol(int title) {
        String title_string = activity.getResources().getString(title);
        titleTextView.setText(title_string);
    }

    public CharSequence getTitle() {
        return titleTextView.getText();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public CharSequence getSummary() {
        return summaryTextView.getText();
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);
    }

}
