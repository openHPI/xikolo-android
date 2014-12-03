package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.ItemAssignment;
import de.xikolo.data.entities.ItemText;
import de.xikolo.data.entities.ItemVideo;
import de.xikolo.util.Config;

public class RetrieveItemDetailJob extends Job {

    public static final String TAG = RetrieveItemDetailJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String courseId;
    private String moduleId;
    private String itemId;
    private String itemType;
    private boolean cache;

    private String token;

    private OnJobResponseListener<Item> mCallback;

    public RetrieveItemDetailJob(OnJobResponseListener<Item> callback, String courseId, String moduleId, String itemId, String itemType, boolean cache, String token) {
        super(new Params(Priority.HIGH).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.courseId = courseId;
        this.moduleId = moduleId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.cache = cache;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | cache " + cache + " | courseId " + courseId + " | moduleId " + moduleId + " | itemId " + itemId + " | itemType " + itemType);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = null;
        if (itemType.equals(Item.TYPE_TEXT)) {
            type = new TypeToken<Item<ItemText>>() {
            }.getType();
        } else if (itemType.equals(Item.TYPE_VIDEO)) {
            type = new TypeToken<Item<ItemVideo>>() {
            }.getType();
        } else if (itemType.equals(Item.TYPE_SELFTEST)
                || itemType.equals(Item.TYPE_ASSIGNMENT)
                || itemType.equals(Item.TYPE_EXAM)) {
            type = new TypeToken<Item<ItemAssignment>>() {
            }.getType();
        }

        String url = Config.API + Config.COURSES + courseId + "/"
                + Config.MODULES + moduleId + "/" + Config.ITEMS + itemId;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        request.setToken(token);

        Object o = request.getResponse();
        if (o != null) {
            Item item = (Item) o;
            if (Config.DEBUG)
                Log.i(TAG, "ItemDetail received");
            mCallback.onResponse(item);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No ItemDetail received");
            mCallback.onCancel();
        }
    }

    @Override
    protected void onCancel() {
        mCallback.onCancel();
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
