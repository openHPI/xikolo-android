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
import android.support.v4.content.ContextCompat;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controllers.main.MainActivity;
import de.xikolo.receivers.CancelDownloadsReceiver;

public class NotificationUtil extends ContextWrapper {

    public static final String DOWNLOADS_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".downloads";

    private NotificationManager notificationManager;

    public NotificationUtil(Context base) {
        super(base);
        createChannels();
    }

    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    public NotificationCompat.Builder getDownloadsNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent deleteIntent = new Intent(this, CancelDownloadsReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
                .setContentTitle("Downloading...")
                .setContentText("Please wait...")
                .setColor(ContextCompat.getColor(this, R.color.apptheme_main))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), android.R.drawable.stat_sys_download))
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notification_downloads_cancel), pendingIntentCancel);
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

}
