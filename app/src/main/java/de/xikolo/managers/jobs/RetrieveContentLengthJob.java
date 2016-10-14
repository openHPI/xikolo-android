package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.network.ApiRequest;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveContentLengthJob extends Job {

    public static final String TAG = RetrieveContentLengthJob.class.getSimpleName();

    private String url;
    private Result<Long> result;

    public RetrieveContentLengthJob(Result<Long> result, String url) {
        super(new Params(Priority.LOW));

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
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
