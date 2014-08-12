package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Type;

import de.xikolo.BuildConfig;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public abstract class ItemDetailManager {

    public static final String TAG = ItemDetailManager.class.getSimpleName();

    private Context mContext;

    public ItemDetailManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestItemDetail(Course course, Module module, Item item, Type type, boolean cache) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestItemDetail() called | cache " + cache);

        JsonRequest request = new JsonRequest(Config.API_SAP + Config.COURSES + course.id + "/"
                + Config.MODULES + module.id + "/" + Config.ITEMS + item.id, type, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (o != null) {
                    Item item = (Item) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "ItemDetail received");
                    onItemDetailRequestReceived(item);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No ItemDetail received");
                    onItemDetailRequestCancelled();
                }
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "ItemDetail Request cancelled");
                onItemDetailRequestCancelled();
            }
        };
        request.setCache(cache);
        if (!NetworkUtil.isOnline(mContext) && cache) {
            request.setCacheOnly(true);
        }
        request.setToken(TokenManager.getAccessToken(mContext));
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onItemDetailRequestReceived(Item item);

    public abstract void onItemDetailRequestCancelled();

}
