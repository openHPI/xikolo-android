package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Type;

import de.xikolo.BuildConfig;
import de.xikolo.dataaccess.JsonRequest;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;
import de.xikolo.util.Network;

public abstract class ItemObjectManager {

    public static final String TAG = ItemObjectManager.class.getSimpleName();

    private Context mContext;

    public ItemObjectManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestItemObject(Course course, Module module, Item item, Type type, boolean cache) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestItemObject() called | cache " + cache);

        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_COURSES + course.id + "/"
                + Config.PATH_MODULES + module.id + "/" + Config.PATH_ITEMS + item.id, type, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (o != null) {
                    Item item = (Item) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "Item received");
                    onItemRequestReceived(item);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No Item received");
                    onItemRequestCancelled();
                }
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "Item Request cancelled");
                onItemRequestCancelled();
            }
        };
        request.setCache(cache);
        if (!Network.isOnline(mContext) && cache) {
            request.setCacheOnly(true);
        }
        request.setToken(TokenManager.getAccessToken(mContext));
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onItemRequestReceived(Item item);

    public abstract void onItemRequestCancelled();

}
