package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.VideoDataAccess;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;

public class RetrieveLocalVideoJob extends Job {

    public static final String TAG = RetrieveLocalVideoJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<VideoItemDetail> result;
    private VideoItemDetail videoItemDetail;

    public RetrieveLocalVideoJob(Result<VideoItemDetail> result, VideoItemDetail videoItemDetail) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.videoItemDetail = videoItemDetail;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            VideoDataAccess videoDataAccess = GlobalApplication.getInstance()
                    .getDataAccessFactory().getVideoDataAccess();
            VideoItemDetail video = videoDataAccess.getVideo(videoItemDetail.id);
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
