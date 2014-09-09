package de.xikolo.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.entities.Module;
import de.xikolo.util.Config;

public class RetrieveModulesJob extends Job {

    public static final String TAG = RetrieveModulesJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String courseId;
    private boolean cache;
    private boolean includeProgress;

    private String token;

    private OnJobResponseListener<List<Module>> mCallback;

    public RetrieveModulesJob(OnJobResponseListener<List<Module>> callback, String courseId, boolean cache, String token) {
        this(callback, courseId, cache, false, token);
    }

    public RetrieveModulesJob(OnJobResponseListener<List<Module>> callback, String courseId, boolean cache, boolean includeProgress, String token) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(TAG));
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.courseId = courseId;
        this.cache = cache;
        this.includeProgress = includeProgress;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | cache " + cache + " | includeProgress " + includeProgress + " | courseId " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<List<Module>>() {
        }.getType();

        String url = Config.API + Config.COURSES + courseId + "/"
                + Config.MODULES + "?include_progress=" + includeProgress;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        request.setToken(token);

        Object o = request.getResponse();
        if (o != null) {
            List<Module> modules = (List<Module>) o;
            if (Config.DEBUG)
                Log.i(TAG, "Modules received (" + modules.size() + ")");
            mCallback.onResponse(modules);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No Modules received");
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
