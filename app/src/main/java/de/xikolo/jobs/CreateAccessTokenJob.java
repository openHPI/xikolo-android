package de.xikolo.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.JsonRequest;
import de.xikolo.entities.AccessToken;
import de.xikolo.util.Config;

public class CreateAccessTokenJob extends Job {

    public static final String TAG = CreateAccessTokenJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String email;
    private String password;

    private OnJobResponseListener<AccessToken> mCallback;

    public CreateAccessTokenJob(OnJobResponseListener<AccessToken> callback, String email, String password) {
        super(new Params(Priority.MID).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.email = email;
        this.password = password;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | email " + email);
    }

    @Override
    public void onRun() throws Throwable {
        Type type = new TypeToken<AccessToken>() {
        }.getType();

        String url = Config.API + Config.AUTHENTICATE + "?email=" + email + "&password=" + URLEncoder.encode(password, "UTF-8");

        JsonRequest request = new JsonRequest(url, type);
        request.setMethod(Config.HTTP_POST);
        request.setCache(false);

        Object o = request.getResponse();
        if (o != null) {
            AccessToken token = (AccessToken) o;
            if (Config.DEBUG)
                Log.i(TAG, "AccessToken created");
            mCallback.onResponse(token);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "AccessToken not created");
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
