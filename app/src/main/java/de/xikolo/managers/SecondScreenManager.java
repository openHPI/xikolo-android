package de.xikolo.managers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.SecondScreenActivity;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.entities.WebSocketMessage;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.Event;

public class SecondScreenManager {

    public static final String TAG = SecondScreenManager.class.getSimpleName();

    public static final int NOTIFICATION_ID = "second_screen_notification".hashCode();

    private Item item;

    private ItemModel itemModel;

    public SecondScreenManager() {
        itemModel = new ItemModel(GlobalApplication.getInstance().getJobManager());

        EventBus.getDefault().register(this);
    }

    public void onEventBackgroundThread(WebSocketManager.WebSocketMessageEvent event) {
        final WebSocketMessage message = event.getWebSocketMessage();

        if (!message.platform().equals("web") || !message.action().startsWith("video_")) {
            // ignore other WebSocket events
            return;
        }

        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item item, DataSource dataSource) {
                if (!item.equals(SecondScreenManager.this.item)) {
                    SecondScreenManager.this.item = item;

                    Context context = GlobalApplication.getInstance();

                    // show notification
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_notification_second_screen)
                                    .setContentTitle(context.getString(R.string.notification_start_second_screen))
                                    .setContentText(item.title)
                                    .setAutoCancel(true)
                                    .setColor(ContextCompat.getColor(context, R.color.apptheme_main));

                    Intent intent = new Intent(context, SecondScreenActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(contentIntent);

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());

                    // post sticky new video event
                    EventBus.getDefault().postSticky(new SecondScreenNewVideoEvent(item, message));
                }
            }
        };

        if (item == null || !item.id.equals(message.payload().get("item_id"))) {
            // new video detected
            Log.d(TAG, "new video detected");
            itemModel.getItemDetail(result, message.payload().get("course_id"), message.payload().get("section_id"), message.payload().get("item_id"), Item.TYPE_VIDEO);
        } else if (item != null && item.id.equals(message.payload().get("item_id"))) {
            // post video updated event
            EventBus.getDefault().post(new SecondScreenUpdateVideoEvent(item, message));

            if (message.action().equals("video_close")) {
                NotificationManager notificationManager = (NotificationManager) GlobalApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFICATION_ID);

                item = null;
            }
        }
    }

    public static class SecondScreenUpdateVideoEvent extends Event {

        private Item<VideoItemDetail> item;

        private WebSocketMessage webSocketMessage;

        public SecondScreenUpdateVideoEvent(Item item, WebSocketMessage webSocketMessage) {
            super();
            this.item = item;
            this.webSocketMessage = webSocketMessage;
        }

        public Item<VideoItemDetail> getItem() {
            return item;
        }

        public WebSocketMessage getWebSocketMessage() {
            return webSocketMessage;
        }

    }

    public static class SecondScreenNewVideoEvent extends SecondScreenUpdateVideoEvent {

        public SecondScreenNewVideoEvent(Item<VideoItemDetail> item, WebSocketMessage webSocketMessage) {
            super(item, webSocketMessage);
        }

    }

}
