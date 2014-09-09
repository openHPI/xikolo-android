package de.xikolo.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.entities.Item;
import de.xikolo.util.Config;

public class RetrieveItemsJob extends Job {

    public static final String TAG = RetrieveItemsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String courseId;
    private String moduleId;
    private boolean cache;

    private String token;

    private OnJobResponseListener<List<Item>> mCallback;

    public RetrieveItemsJob(OnJobResponseListener<List<Item>> callback, String courseId, String moduleId, boolean cache, String token) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(TAG));
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.courseId = courseId;
        this.moduleId = moduleId;
        this.cache = cache;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | cache " + cache + " | courseId " + courseId + " | moduleId " + moduleId);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        String url = Config.API + Config.COURSES + courseId + "/"
                + Config.MODULES + moduleId + "/" + Config.ITEMS;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        request.setToken(token);

        Object o = request.getResponse();
        if (o != null) {
            List<Item> items = (List<Item>) o;
            if (Config.DEBUG)
                Log.i(TAG, "Items received (" + items.size() + ")");
            mCallback.onResponse(items);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No Item received");
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
