package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.data.net.HttpRequest;
import de.xikolo.util.Config;

public class CreateEnrollmentJob extends Job {

    public static final String TAG = CreateEnrollmentJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String enrollmentId;
    private String token;

    private OnJobResponseListener<Void> mCallback;

    public CreateEnrollmentJob(OnJobResponseListener<Void> callback, String enrollmentId, String token) {
        super(new Params(Priority.MID).requireNetwork());
        id = jobCounter.incrementAndGet();

        mCallback = callback;

        this.enrollmentId = enrollmentId;
        this.token = token;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG)
            Log.i(TAG, TAG + " added | enrollmentId " + enrollmentId);
    }

    @Override
    public void onRun() throws Throwable {
        String url = Config.API + Config.USER + Config.ENROLLMENTS + "?course_id=" + enrollmentId;

        HttpRequest request = new HttpRequest(url);
        request.setMethod(Config.HTTP_POST);
        request.setToken(token);
        request.setCache(false);

        Object o = request.getResponse();
        if (o != null) {
            if (Config.DEBUG)
                Log.i(TAG, "Enrollment created");
            mCallback.onResponse(null);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "Enrollment not created");
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
