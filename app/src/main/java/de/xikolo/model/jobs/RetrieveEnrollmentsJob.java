package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.entities.Enrollment;
import de.xikolo.util.Config;

public class RetrieveEnrollmentsJob extends Job {

    public static final String TAG = RetrieveEnrollmentsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private boolean cache;
    private String token;

    private OnJobResponseListener<List<Enrollment>> mCallback;

    public RetrieveEnrollmentsJob(OnJobResponseListener<List<Enrollment>> callback, boolean cache, String token) {
        super(new Params(Priority.HIGH).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.cache = cache;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | cache " + cache);
        }
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<List<Enrollment>>() {
        }.getType();

        String url = Config.API + Config.USER + Config.ENROLLMENTS;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        request.setToken(token);

        Object o = request.getResponse();
        if (o != null) {
            List<Enrollment> enrollments = (List<Enrollment>) o;
            if (Config.DEBUG)
                Log.i(TAG, "Enrollments received (" + enrollments.size() + ")");
            mCallback.onResponse(enrollments);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No Enrollments received");
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
