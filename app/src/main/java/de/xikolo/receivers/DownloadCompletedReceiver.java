package de.xikolo.receivers;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.DownloadsActivity;
import de.xikolo.events.DownloadCompletedEvent;
import de.xikolo.models.Download;
import de.xikolo.network.DownloadHelper;
import de.xikolo.storages.preferences.NotificationStorage;
import de.xikolo.storages.preferences.StorageType;

public class DownloadCompletedReceiver extends BroadcastReceiver {

    public static final int DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID = 1;

    public static final String DOWNLOAD_COMPLETED_NOTIFICATION_GROUP = "download_completed";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            Download dl = DownloadHelper.getDownloadForId(downloadId);

            if (dl != null) {
                EventBus.getDefault().post(new DownloadCompletedEvent(dl));

                NotificationStorage notificationStorage = (NotificationStorage) GlobalApplication.getStorage(StorageType.NOTIFICATION);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    notificationStorage.addDownloadNotification(dl.title);
                    showNotification(context, dl.title);

                    List<String> downloadList = notificationStorage.getDownloadNotifications();
                    if (downloadList.size() > 1) {
                        showSummaryNotification(context, downloadList);
                    }
                } else { // Notifications with group doesn't work before Lollipop
                    notificationStorage.addDownloadNotification(dl.title);
                    List<String> downloadList = notificationStorage.getDownloadNotifications();
                    showSummaryNotification(context, downloadList);
                }
            }
        }
    }

    private void showNotification(Context context, String title) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_download)
                        .setContentTitle(context.getString(R.string.notification_download_completed))
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
                        .setColor(ContextCompat.getColor(context, R.color.apptheme_main));

        mBuilder.setContentIntent(createContentIntent(context, DownloadsActivity.class,NotificationDeletedReceiver.KEY_TITLE, title));

        mBuilder.setDeleteIntent(createDeleteIntent(context, NotificationDeletedReceiver.KEY_TITLE, title));

        getNotificationManager(context).notify(title.hashCode(), mBuilder.build());
    }

    private void showSummaryNotification(Context context, List<String> notifications) {
        String title;
        if (notifications.size() > 1) {
            title = String.format(context.getString(R.string.notification_multiple_downloads_completed), notifications.size());
        } else {
            title = context.getString(R.string.notification_download_completed);
        }

        NotificationCompat.Builder mSummaryBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_download)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setColor(ContextCompat.getColor(context, R.color.apptheme_main));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText(context.getString(R.string.app_name));

        for (String notification : notifications) {
            inboxStyle.addLine(notification);
        }
        mSummaryBuilder.setStyle(inboxStyle);

        mSummaryBuilder.setContentIntent(createContentIntent(context, DownloadsActivity.class, NotificationDeletedReceiver.KEY_ALL, "true"));

        mSummaryBuilder.setDeleteIntent(createDeleteIntent(context, NotificationDeletedReceiver.KEY_ALL, "true"));

        getNotificationManager(context).notify(DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID, mSummaryBuilder.build());
    }

    private NotificationManagerCompat getNotificationManager(Context context) {
        return NotificationManagerCompat.from(context);
    }

    private PendingIntent createContentIntent(Context context, Class parentActivityClass, String extraKey, String extraValue) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, parentActivityClass);

        resultIntent.putExtra(extraKey, extraValue);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(parentActivityClass);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return resultPendingIntent;
    }

    private PendingIntent createDeleteIntent(Context context, String extraKey, String extraValue) {
        Intent deleteIntent = new Intent(NotificationDeletedReceiver.INTENT_ACTION_NOTIFICATION_DELETED);
        deleteIntent.putExtra(extraKey, extraValue);
        return PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
