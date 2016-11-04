package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.VideoItemDetail;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.VideoDataAdapter;
import de.xikolo.utils.Config;

public class UpdateLocalVideoJob extends Job {

    public static final String TAG = UpdateLocalVideoJob.class.getSimpleName();

    private VideoItemDetail videoItemDetail;
    private Result<Void> result;

    public UpdateLocalVideoJob(Result<Void> result, VideoItemDetail videoItemDetail) {
        super(new Params(Priority.MID));

        this.result = result;
        this.videoItemDetail = videoItemDetail;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            VideoDataAdapter videoDataAdapter = (VideoDataAdapter) GlobalApplication.getDataAdapter(DataType.VIDEO);
            videoDataAdapter.addOrUpdate(videoItemDetail);
            result.success(null, Result.DataSource.LOCAL);
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
