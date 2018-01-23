package de.xikolo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import java.util.List;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controllers.downloads.DownloadsActivity;
import de.xikolo.controllers.main.MainActivity;
import de.xikolo.controllers.second_screen.SecondScreenActivity;
import de.xikolo.models.Download;
import de.xikolo.receivers.CancelDownloadsReceiver;
import de.xikolo.receivers.NotificationDeletedReceiver;
import de.xikolo.storages.NotificationStorage;

public class NotificationUtil extends ContextWrapper {

    public static final String DOWNLOADS_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".downloads";

    public static final String SECOND_SCREEN_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".second_screen";

    public static final int DOWNLOAD_RUNNING_NOTIFICATION_ID = 1000;

    public static final int DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID = 1001;

    public static final int SECOND_SCREEN_NOTIFICATION_ID = 1002;

    public static final String DOWNLOAD_COMPLETED_NOTIFICATION_GROUP = "download_completed";

    private NotificationManager notificationManager;

    public static final String NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE = "key_notification_deleted_title";

    public static final String NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL = "key_notification_deleted_all";

    public NotificationUtil(Context base) {
        super(base);
        createChannels();
    }

    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    public void cancel(int id) {
        getManager().cancel(id);
    }

    public NotificationCompat.Builder getDownloadRunningNotification(List<String> notifications) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent deleteIntent = new Intent(this, CancelDownloadsReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
                .setColor(ContextCompat.getColor(this, R.color.apptheme_main))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), android.R.drawable.stat_sys_download))
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notification_downloads_cancel), pendingIntentCancel);

        if (notifications.size() == 1) {
            return builder.setContentTitle(notifications.get(0));
        } else {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .setBigContentTitle(getString(R.string.notification_multiple_downloads_running, notifications.size()));

            for (String notification : notifications) {
                inboxStyle.addLine(notification);
            }

            return builder.setStyle(inboxStyle);
        }
    }

    public void showDownloadCompletedNotification(Download download) {
        NotificationStorage notificationStorage = new NotificationStorage();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            notificationStorage.addDownloadNotification(download.title);
            notify(download.title.hashCode(), getDownloadCompletedNotification(download).build());

            List<String> downloadList = notificationStorage.getDownloadNotifications();
            if (downloadList.size() > 1) {
                showDownloadSummaryNotification(downloadList);
            }
        } else { // Notifications with group doesn't work before Lollipop
            notificationStorage.addDownloadNotification(download.title);
            List<String> downloadList = notificationStorage.getDownloadNotifications();
            showDownloadSummaryNotification(downloadList);
        }
    }

    private NotificationCompat.Builder getDownloadCompletedNotification(Download download) {
        return new NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_download_completed))
                .setContentText(download.title)
                .setColor(ContextCompat.getColor(this, R.color.apptheme_main))
                .setSmallIcon(R.drawable.ic_notification_download)
                .setAutoCancel(true)
                .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
                .setContentIntent(createDownloadCompletedContentIntent(DownloadsActivity.class, NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE, download.title))
                .setDeleteIntent(createDownloadCompletedDeleteIntent(NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE, download.title));
    }

    private void showDownloadSummaryNotification(List<String> notifications) {
        notify(DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID, getDownloadSummaryNotification(notifications).build());
    }

    private NotificationCompat.Builder getDownloadSummaryNotification(List<String> notifications) {
        String title;
        if (notifications.size() > 1) {
            title = String.format(getString(R.string.notification_multiple_downloads_completed), notifications.size());
        } else {
            title = getString(R.string.notification_download_completed);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(title);

        for (String notification : notifications) {
            inboxStyle.addLine(notification);
        }

        return new NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(getString(R.string.app_name))
                .setColor(ContextCompat.getColor(this, R.color.apptheme_main))
                .setSmallIcon(R.drawable.ic_notification_download)
                .setAutoCancel(true)
                .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setStyle(inboxStyle)
                .setContentIntent(createDownloadCompletedContentIntent(DownloadsActivity.class, NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL, "true"))
                .setDeleteIntent(createDownloadCompletedDeleteIntent(NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL, "true"));
    }

    public void showSecondScreenNotification(String title) {
        notify(SECOND_SCREEN_NOTIFICATION_ID, getSecondScreenNotification(title).build());
    }

    public void cancelSecondScreenNotification() {
        cancel(SECOND_SCREEN_NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getSecondScreenNotification(String title) {
        Intent intent = new Intent(this, SecondScreenActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, SECOND_SCREEN_CHANNEL_ID)
                .setContentText(title)
                .setContentTitle(getString(R.string.notification_start_second_screen))
                .setColor(ContextCompat.getColor(this, R.color.apptheme_main))
                .setSmallIcon(R.drawable.ic_notification_second_screen)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[0])
                .setContentIntent(contentIntent);
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel downloadsChannel = new NotificationChannel(
                    DOWNLOADS_CHANNEL_ID,
                    getString(R.string.notification_channel_downloads),
                    NotificationManager.IMPORTANCE_LOW
            );
            downloadsChannel.setShowBadge(false);

            getManager().createNotificationChannel(downloadsChannel);

            NotificationChannel secondScreenChannel = new NotificationChannel(
                    SECOND_SCREEN_CHANNEL_ID,
                    getString(R.string.notification_channel_second_screen),
                    NotificationManager.IMPORTANCE_HIGH
            );
            secondScreenChannel.setShowBadge(false);

            getManager().createNotificationChannel(secondScreenChannel);
        }
    }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            synchronized (NotificationUtil.class) {
                if (notificationManager == null) {
                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                }
            }
        }
        return notificationManager;
    }

    private PendingIntent createDownloadCompletedContentIntent(Class parentActivityClass, String extraKey, String extraValue) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, parentActivityClass);

        resultIntent.putExtra(extraKey, extraValue);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
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

    private PendingIntent createDownloadCompletedDeleteIntent(String extraKey, String extraValue) {
        Intent deleteIntent = new Intent(this, NotificationDeletedReceiver.class);
        deleteIntent.setAction(NotificationDeletedReceiver.INTENT_ACTION_NOTIFICATION_DELETED);
        deleteIntent.putExtra(extraKey, extraValue);
        return PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void deleteDownloadNotificationsFromIntent(Intent intent) {
        NotificationStorage notificationStorage = new NotificationStorage();

        String title = intent.getStringExtra(NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE);
        if (title != null) {
            notificationStorage.deleteDownloadNotification(title);
        } else if (intent.getStringExtra(NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL) != null) {
            notificationStorage.delete();
        }
    }

}
