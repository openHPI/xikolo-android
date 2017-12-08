package de.xikolo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.xikolo.utils.NotificationUtil;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_NOTIFICATION_DELETED = "de.xikolo.intent.action.NOTIFICATION_DELETED";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();

        if (INTENT_ACTION_NOTIFICATION_DELETED.equals(action)) {
            NotificationUtil.deleteDownloadNotificationsFromIntent(intent);
        }
    }

}
