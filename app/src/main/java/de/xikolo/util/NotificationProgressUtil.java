package de.xikolo.util;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat.Builder;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;

/**
 * @author Denis Fyedyayev, 6/21/15.
 */
public class NotificationProgressUtil {

    private static NotificationProgressUtil instance;
    private ArrayList<Notification> notifications;
    private AtomicInteger atomicInteger;

    private NotificationManager notificationManager;

    public static NotificationProgressUtil getInstance() {
        if (instance == null) {
            instance = new NotificationProgressUtil();
        }
        return instance;
    }

    private NotificationProgressUtil() {
        notifications = new ArrayList<>();
        atomicInteger = new AtomicInteger();
        notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private Context getContext() {
        return GlobalApplication.getInstance();
    }

    private Builder getBuilder(int id) {
        for (Notification notification : notifications) {
            if (notification.id == id) {
                return notification.builder;
            }
        }
        return null;
    }

    public int show(String title, String text, int iconResource) {
        int id = atomicInteger.incrementAndGet();
        Builder builder = new Builder(getContext())
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(iconResource);
        notificationManager.notify(id, builder.build());
        notifications.add(new Notification(builder, id));
        return id;
    }

    public void setProgress(int id, int downloaded, int total) {
        Builder builder = getBuilder(id);
        if (builder != null) {
            builder.setProgress(total, downloaded, false);
            notificationManager.notify(id, builder.build());
        }
    }

    public void onCompleted(int id, String completeMessage) {
        Builder builder = getBuilder(id);
        if (builder != null) {
            builder.setContentTitle(completeMessage).setProgress(0, 0, false);
            notificationManager.notify(id, builder.build());
            for (Notification notification : notifications) {
                if (notification.id == id) {
                    notifications.remove(notification);
                    break;
                }
            }
        }
    }

    private class Notification {
        public Builder builder;
        public int id;

        public Notification(Builder builder, int id) {
            this.builder = builder;
            this.id = id;
        }
    }
}
