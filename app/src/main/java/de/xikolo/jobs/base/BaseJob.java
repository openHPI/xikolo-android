package de.xikolo.jobs.base;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

public abstract class BaseJob extends Job {

    protected static int PRIORITY_LOW = 0;
    protected static int PRIORITY_MID = 500;
    protected static int PRIORITY_HIGH = 1000;

    protected JobCallback callback;

    public BaseJob(Params params, JobCallback callback) {
        super(params);
        this.callback = callback;
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        if (callback != null) callback.error(JobCallback.ErrorCode.CANCEL);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
