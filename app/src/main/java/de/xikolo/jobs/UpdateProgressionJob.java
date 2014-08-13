package de.xikolo.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.HttpRequest;
import de.xikolo.util.Config;

public class UpdateProgressionJob extends Job {

    public static final String TAG = UpdateProgressionJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String itemId;
    private String token;

    private OnJobResponseListener<Void> mCallback;

    public UpdateProgressionJob(OnJobResponseListener<Void> callback, String itemId, String token) {
        // TODO make persistent
        super(new Params(Priority.LOW).requireNetwork().groupBy(TAG));
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.itemId = itemId;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | itemId " + itemId);
    }

    @Override
    public void onRun() throws Throwable {
        String url = Config.API_SAP + Config.USER + Config.PROGRESSIONS + itemId;

        HttpRequest request = new HttpRequest(url);
        request.setMethod(Config.HTTP_PUT);
        request.setToken(token);
        request.setCache(false);

        Object o = request.getResponse();
        if (o != null) {
            if (Config.DEBUG)
                Log.i(TAG, "Progression updated");
            mCallback.onResponse(null);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "Progression not updated");
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
