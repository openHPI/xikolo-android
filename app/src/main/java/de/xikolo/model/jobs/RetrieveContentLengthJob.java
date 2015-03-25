package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class RetrieveContentLengthJob extends Job {

    public static final String TAG = RetrieveContentLengthJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Long> result;
    private String url;

    public RetrieveContentLengthJob(Result<Long> result, String url) {
        super(new Params(Priority.LOW));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.url = url;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | url " + url);
    }

    @Override
    public void onRun() throws Throwable {
        if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_NETWORK);
        } else {
            HttpRequest request = new HttpRequest(url);
            request.setCache(false);

            Long length = request.getContentLength();
            result.success(length, Result.DataSource.NETWORK);
        }
    }

    @Override
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
