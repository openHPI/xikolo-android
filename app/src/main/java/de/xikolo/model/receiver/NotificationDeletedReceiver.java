package de.xikolo.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.xikolo.data.preferences.NotificationPreferences;
import de.xikolo.data.preferences.PreferencesFactory;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_NOTIFICATION_DELETED = "de.xikolo.intent.action.NOTIFICATION_DELETED";

    public static final String KEY_TITLE = "key_notification_deleted_title";
    public static final String KEY_ALL = "key_notification_deleted_all";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();

        if (INTENT_ACTION_NOTIFICATION_DELETED.equals(action)) {
            PreferencesFactory preferencesFactory = new PreferencesFactory(context);
            NotificationPreferences notificationPreferences = preferencesFactory.getNotificationPreferences();

            String title = intent.getStringExtra(KEY_TITLE);
            if (title != null) {
                notificationPreferences.deleteDownloadNotification(title);
            } else if (intent.getStringExtra(KEY_ALL) != null) {
                notificationPreferences.deleteAllDownloadNotifications();
            }
        }
    }

}
