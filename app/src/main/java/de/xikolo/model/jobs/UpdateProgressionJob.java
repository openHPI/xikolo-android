package de.xikolo.model.jobs;

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

    public UpdateProgressionJob(String itemId, String token) {
        super(new Params(Priority.LOW).persist().requireNetwork());
        id = jobCounter.incrementAndGet();

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
        String url = Config.API + Config.USER + Config.PROGRESSIONS + itemId;

        HttpRequest request = new HttpRequest(url);
        request.setMethod(Config.HTTP_PUT);
        request.setToken(token);
        request.setCache(false);

        Object o = request.getResponse();
        if (o != null) {
            if (Config.DEBUG)
                Log.i(TAG, "Progression updated");
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "Progression not updated");
        }
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
