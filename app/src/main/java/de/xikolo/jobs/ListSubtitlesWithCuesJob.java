package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.SubtitleCue;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListSubtitlesWithCuesJob extends BaseJob {

    public static final String TAG = ListSubtitlesWithCuesJob.class.getSimpleName();

    private String videoId;

    public ListSubtitlesWithCuesJob(JobCallback callback, String videoId) {
        super(new Params(PRIORITY_HIGH), callback);

        this.videoId = videoId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | video.id " + videoId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {
                final Response<SubtitleTrack.JsonModel[]> response = ApiService.getInstance().listSubtitlesWithCuesForVideo(
                        UserManager.getTokenAsHeader(),
                        videoId
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Subtitles received");

                    String[] ids = Sync.Data.with(SubtitleTrack.class, response.body())
                            .addFilter("videoId", videoId)
                            .run();
                    Sync.Included.with(SubtitleCue.class, response.body())
                            .addFilter("subtitleId", ids)
                            .run();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching subtitle list");
                    if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
                }
            } else {
                if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
