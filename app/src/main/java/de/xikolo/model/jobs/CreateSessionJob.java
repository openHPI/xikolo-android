package de.xikolo.model.jobs;

import android.util.Log;
import android.webkit.CookieManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import de.xikolo.data.net.HttpRequest;
import de.xikolo.util.Config;

public class CreateSessionJob extends Job {

    public static final String TAG = CreateSessionJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String token;

    private OnJobResponseListener<Void> mCallback;

    public CreateSessionJob(OnJobResponseListener<Void> callback, String token) {
        super(new Params(Priority.MID).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        String url = Config.URI + Config.LOGIN;

        HttpRequest request = new HttpRequest(url);
        request.setToken(token);
        request.setMethod(Config.HTTP_POST);
        request.setCache(false);

        HttpsURLConnection urlConnection = request.createConnection();
        if (urlConnection != null) {
            boolean hasSession = false;
            List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) for (String cookieTemp : cookieList) {
                if (cookieTemp.startsWith("_normandy_session")) {
                    CookieManager.getInstance().setCookie(urlConnection.getURL().toString(), cookieTemp);
                    hasSession = true;
                }
            }
            if (hasSession) {
                if (Config.DEBUG)
                    Log.i(TAG, "Session created");
                mCallback.onResponse(null);
            } else {
                if (Config.DEBUG)
                    Log.w(TAG, "Session not created");
                mCallback.onCancel();
            }
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "Session not created");
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
