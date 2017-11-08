package de.xikolo.managers;

import android.annotation.TargetApi;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.App;
import de.xikolo.events.base.Event;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Item;
import de.xikolo.models.Video;
import de.xikolo.models.WebSocketMessage;
import de.xikolo.utils.NotificationUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SecondScreenManager {

    public static final String TAG = SecondScreenManager.class.getSimpleName();

    private String courseId;

    private String sectionId;

    private String itemId;

    private CourseManager courseManager;

    private ItemManager itemManager;

    private NotificationUtil notificationUtil;

    private boolean isRequesting;

    public SecondScreenManager() {
        courseManager = new CourseManager();
        itemManager = new ItemManager();

        notificationUtil = new NotificationUtil(App.getInstance());

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
                        notificationUtil.cancelSecondScreenNotification();

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
                Item item = Item.get(itemId);

                // show notification
                notificationUtil.showSecondScreenNotification(item.title);

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
