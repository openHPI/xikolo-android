package de.xikolo.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.HttpRequest;
import de.xikolo.util.Config;

public class DeleteEnrollmentJob extends Job {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private OnJobResponseListener<Void> mCallback;

    private String enrollmentId;
    private String token;

    public DeleteEnrollmentJob(OnJobResponseListener<Void> callback, String enrollmentId, String token) {
        super(new Params(Priority.MID).requireNetwork().groupBy(TAG));
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.enrollmentId = enrollmentId;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | enrollmentId " + enrollmentId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        String url = Config.API + Config.USER + Config.ENROLLMENTS + enrollmentId;

        HttpRequest request = new HttpRequest(url);
        request.setMethod(Config.HTTP_DELETE);
        request.setToken(token);
        request.setCache(false);

        Object o = request.getResponse();
        if (o != null) {
            if (Config.DEBUG)
                Log.i(TAG, "Enrollment deleted");
            mCallback.onResponse(null);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "Enrollment not deleted");
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
