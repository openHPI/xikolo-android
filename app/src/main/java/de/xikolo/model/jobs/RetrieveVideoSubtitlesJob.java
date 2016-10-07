package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.data.entities.Subtitle;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveVideoSubtitlesJob extends Job {

    public static final String TAG = RetrieveVideoSubtitlesJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Subtitle>> result;

    private String courseId;
    private String moduleId;
    private String videoId;

    public RetrieveVideoSubtitlesJob(Result<List<Subtitle>> result, String courseId, String moduleId, String videoId) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        if (courseId == null || moduleId == null || videoId == null) {
            throw new WrongParameterException();
        }

        this.result = result;
        this.courseId = courseId;
        this.moduleId = moduleId;
        this.videoId = videoId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | course.id " + courseId + " | module.id " + moduleId + " | item.id " + videoId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS + videoId + "/" + Config.SUBTITLES;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<List<Subtitle>>(){}.getType();
                    List<Subtitle> subtitleList = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Subtitles received");

                    result.success(subtitleList, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No Subtitles received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                result.error(Result.ErrorCode.NO_NETWORK);
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
