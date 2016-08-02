package de.xikolo.managers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.SecondScreenActivity;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.WebSocketMessage;
import de.xikolo.data.parser.GsonHelper;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;

public class SecondScreenManager {

    public static final String TAG = SecondScreenManager.class.getSimpleName();

    private Item item;

    private ItemModel itemModel;

    public SecondScreenManager() {
        itemModel = new ItemModel(GlobalApplication.getInstance().getJobManager());

        EventBus.getDefault().register(this);
    }

    public void onEventBackgroundThread(WebSocketManager.WebSocketMessageEvent event) {
        WebSocketMessage message = GsonHelper.create().fromJson(event.getMessage(), WebSocketMessage.class);

        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item item, DataSource dataSource) {
                SecondScreenManager.this.item = item;

                Context context = GlobalApplication.getInstance();

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
                notificationManager.notify(item.title.hashCode(), builder.build());
            }
        };

        if (item == null || !item.id.equals(message.resourceId())) {
            itemModel.getItemDetail(result, message.payload().get("course_id"), message.payload().get("section_id"), message.resourceId(), Item.TYPE_VIDEO);
        }
    }

}
