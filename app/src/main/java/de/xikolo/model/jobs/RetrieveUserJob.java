package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.entities.User;
import de.xikolo.util.Config;

public class RetrieveUserJob extends Job {

    public static final String TAG = RetrieveUserJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private boolean cache;
    private String token;

    private OnJobResponseListener<User> mCallback;

    public RetrieveUserJob(OnJobResponseListener<User> callback, boolean cache, String token) {
        super(new Params(Priority.HIGH).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.cache = cache;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | cache " + cache);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<User>() {
        }.getType();

        String url = Config.API + Config.USER;

        JsonRequest request = new JsonRequest(url, type);
        request.setCache(cache);
        request.setToken(token);

        Object o = request.getResponse();
        if (o != null) {
            User user = (User) o;
            if (Config.DEBUG)
                Log.i(TAG, "User received: " + user.first_name);
            mCallback.onResponse(user);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No User received");
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
