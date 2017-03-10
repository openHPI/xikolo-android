package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.controllers.exceptions.WrongParameterException;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Subtitle;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveVideoSubtitlesJob extends Job {

    public static final String TAG = RetrieveVideoSubtitlesJob.class.getSimpleName();

    private String courseId;
    private String moduleId;
    private String videoId;
    private Result<List<Subtitle>> result;

    public RetrieveVideoSubtitlesJob(Result<List<Subtitle>> result, String courseId, String moduleId, String videoId) {
        super(new Params(Priority.HIGH));

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
        if (!UserManager.isAuthorized()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS + videoId + "/" + Config.SUBTITLES;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = TypeToken.getParameterized(List.class, Subtitle.class).getType();
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
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
