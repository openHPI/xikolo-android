package de.xikolo.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.entities.Course;
import de.xikolo.util.Config;

public class RetrieveCoursesJob extends Job {

    public static final String TAG = RetrieveCoursesJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private boolean cache;
    private boolean includeProgress;

    private String token;

    private OnJobResponseListener<List<Course>> mCallback;

    public RetrieveCoursesJob(OnJobResponseListener<List<Course>> callback, boolean cache) {
        this(callback, cache, false, null);
    }

    public RetrieveCoursesJob(OnJobResponseListener<List<Course>> callback, boolean cache, boolean includeProgress, String token) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(TAG));
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.cache = cache;
        this.includeProgress = includeProgress;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | cache " + cache + " | includeProgress " + includeProgress);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<List<Course>>() {
        }.getType();

        String url = Config.API_SAP + Config.COURSES + "?include_progress=" + includeProgress;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        if (includeProgress) {
            request.setToken(token);
        }

        Object o = request.getResponse();
        if (o != null) {
            List<Course> courses = (List<Course>) o;
            if (Config.DEBUG)
                Log.i(TAG, "Courses received (" + courses.size() + ")");
            mCallback.onResponse(courses);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No Courses received");
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
