package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.SubtitleCue;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
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

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for (SubtitleTrack.JsonModel subtitleModel : response.body()) {
                                realm.copyToRealmOrUpdate(subtitleModel.convertToRealmObject());

                                if (subtitleModel.cues != null && subtitleModel.cues.get(subtitleModel.getContext()) != null) {
                                    for (SubtitleCue.JsonModel cueModel : subtitleModel.cues.get(subtitleModel.getContext())) {
                                        SubtitleCue cue = cueModel.convertToRealmObject();
                                        realm.copyToRealmOrUpdate(cue);
                                    }
                                }
                            }
                        }
                    });
                    realm.close();

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
