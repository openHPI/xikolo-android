package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

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
        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            Response response = new ApiRequest(url)
                    .head()
                    .execute();
            if (response.isSuccessful()) {
                Long length = Long.parseLong(response.header("Content-Length", "0"));
                response.close();

                result.success(length, Result.DataSource.NETWORK);
            } else {
                result.error(Result.ErrorCode.NO_RESULT);
            }
        } else {
            result.error(Result.ErrorCode.NO_NETWORK);
        }
    }

    @Override
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
