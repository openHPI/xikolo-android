package de.xikolo.managers.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.VideoDataAdapter;
import de.xikolo.models.VideoItemDetail;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.utils.Config;

public class RetrieveLocalVideoJob extends Job {

    public static final String TAG = RetrieveLocalVideoJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String videoId;
    private Result<VideoItemDetail> result;

    public RetrieveLocalVideoJob(Result<VideoItemDetail> result, String videoId) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.videoId = videoId;
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
            VideoItemDetail video = videoDataAdapter.get(videoId);
            if (video != null) {
                result.success(video, Result.DataSource.LOCAL);
            } else {
                result.error(Result.ErrorCode.NO_RESULT);
            }
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
