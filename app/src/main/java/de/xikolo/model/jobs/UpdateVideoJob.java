package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.VideoDataAccess;
import de.xikolo.data.entities.AssignmentItemDetail;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.LtiItemDetail;
import de.xikolo.data.entities.PeerAssessmentItemDetail;
import de.xikolo.data.entities.TextItemDetail;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class UpdateVideoJob extends Job {

    public static final String TAG = UpdateVideoJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Void> result;
    private VideoDataAccess videoDataAccess;
    VideoItemDetail videoItemDetail;

    public UpdateVideoJob(Result<Void> result, VideoDataAccess videoDataAccess, VideoItemDetail videoItemDetail) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.videoDataAccess = videoDataAccess;
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
            videoDataAccess.addOrUpdateVideo(videoItemDetail);
            result.success(null, Result.DataSource.LOCAL);
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
