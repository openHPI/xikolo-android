package de.xikolo.managers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.controllers.second_screen.SecondScreenActivity;
import de.xikolo.events.base.Event;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Item;
import de.xikolo.models.Video;
import de.xikolo.models.WebSocketMessage;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SecondScreenManager {

    public static final String TAG = SecondScreenManager.class.getSimpleName();

    public static final int NOTIFICATION_ID = "second_screen_notification".hashCode();

    private String courseId;

    private String sectionId;

    private String itemId;

    private CourseManager courseManager;

    private ItemManager itemManager;

    private boolean isRequesting;

    public SecondScreenManager() {
        courseManager = new CourseManager();
        itemManager = new ItemManager();

        EventBus.getDefault().register(this);

        isRequesting = false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onWebSocketMessageEvent(WebSocketManager.WebSocketMessageEvent event) {
        final WebSocketMessage message = event.getWebSocketMessage();

        if (!message.platform.equals("web") || !message.action.startsWith("video_")) {
            // ignore other WebSocket events
            return;
        }

        synchronized (App.class) {
            String newItemId = message.payload.get("item_id");

            if (!isRequesting) {
                if (itemId == null || !itemId.equals(newItemId)) {
                    // new video detected
                    isRequesting = true;

                    itemId = newItemId;
                    sectionId = message.payload.get("section_id");
                    courseId = message.payload.get("course_id");

                    courseManager.requestCourseWithSections(courseId, courseCallback(message));
                } else {
                    // post video updated event
                    EventBus.getDefault().post(new SecondScreenUpdateVideoEvent(courseId, sectionId, itemId, message));

                    if (message.action.equals("video_close")) {
                        NotificationManager notificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NOTIFICATION_ID);

                        EventBus.getDefault().removeStickyEvent(SecondScreenNewVideoEvent.class);

                        courseId = null;
                        sectionId = null;
                        itemId = null;
                    }
                }
            }
        }
    }

    private JobCallback courseCallback(final WebSocketMessage message) {
        return new JobCallback() {
            @Override
            protected void onSuccess() {
                itemManager.requestItemsWithContentForSection(sectionId, itemCallback(message));
            }

            @Override
            protected void onError(ErrorCode code) {
                errorWhileRequesting();
            }
        };
    }

    private JobCallback itemCallback(final WebSocketMessage message) {
        return new JobCallback() {
            @Override
            public void onSuccess() {
                Video video = Video.getForItemId(itemId);
                itemManager.requestSubtitlesWithCuesForVideo(video.id, subtitleCallback(message));
            }

            @Override
            public void onError(ErrorCode code) {
                errorWhileRequesting();
            }
        };
    }

    private JobCallback subtitleCallback(final WebSocketMessage message) {
        return new JobCallback() {
            @Override
            protected void onSuccess() {
                Context context = App.getInstance();

                Item item = Item.get(itemId);

                // show notification
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notification_second_screen)
                                .setContentTitle(context.getString(R.string.notification_start_second_screen))
                                .setContentText(item.title)
                                .setAutoCancel(true)
                                .setColor(ContextCompat.getColor(context, R.color.apptheme_main))
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVibrate(new long[0]);

                Intent intent = new Intent(context, SecondScreenActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(contentIntent);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());

                // post sticky new video event
                EventBus.getDefault().postSticky(new SecondScreenNewVideoEvent(courseId, sectionId, itemId, message));

                isRequesting = false;
            }

            @Override
            protected void onError(ErrorCode code) {
                errorWhileRequesting();
            }
        };
    }

    private void errorWhileRequesting() {
        isRequesting = false;
        courseId = null;
        sectionId = null;
        itemId = null;
    }

    public static class SecondScreenUpdateVideoEvent extends Event {

        public String courseId;

        public String sectionId;

        public String itemId;

        public WebSocketMessage webSocketMessage;

        public SecondScreenUpdateVideoEvent(String courseId, String sectionId, String itemId, WebSocketMessage webSocketMessage) {
            super();
            this.courseId = courseId;
            this.sectionId = sectionId;
            this.itemId = itemId;
            this.webSocketMessage = webSocketMessage;
        }

    }

    public static class SecondScreenNewVideoEvent extends SecondScreenUpdateVideoEvent {

        public SecondScreenNewVideoEvent(String courseId, String sectionId, String itemId, WebSocketMessage webSocketMessage) {
            super(courseId, sectionId, itemId, webSocketMessage);
        }

    }

}
