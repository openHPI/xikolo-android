package de.xikolo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.xikolo.GlobalApplication;
import de.xikolo.storages.preferences.NotificationStorage;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_NOTIFICATION_DELETED = "de.xikolo.intent.action.NOTIFICATION_DELETED";

    public static final String KEY_TITLE = "key_notification_deleted_title";
    public static final String KEY_ALL = "key_notification_deleted_all";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();

        if (INTENT_ACTION_NOTIFICATION_DELETED.equals(action)) {
            NotificationStorage notificationStorage = (NotificationStorage) GlobalApplication.getStorage(StorageType.NOTIFICATION);

            String title = intent.getStringExtra(KEY_TITLE);
            if (title != null) {
                notificationStorage.deleteDownloadNotification(title);
            } else if (intent.getStringExtra(KEY_ALL) != null) {
                notificationStorage.deleteAllDownloadNotifications();
            }
        }
    }

}
