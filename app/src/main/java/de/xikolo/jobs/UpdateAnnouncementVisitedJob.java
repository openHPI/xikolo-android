package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.models.base.Sync;
import de.xikolo.network.ApiService;
import io.realm.Realm;
import retrofit2.Response;

public class UpdateAnnouncementVisitedJob extends BaseJob {

    public static final String TAG = UpdateAnnouncementVisitedJob.class.getSimpleName();

    private String announcementId;

    public UpdateAnnouncementVisitedJob(String announcementId) {
        super(new Params(PRIORITY_LOW).requireNetwork().persist(), null);

        this.announcementId = announcementId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | announcement.id " + announcementId);

        Sync.Update.with(Announcement.class, announcementId)
                .setBeforeCommitCallback(new Sync.BeforeCommitCallback<Announcement>() {
                    @Override
                    public void beforeCommit(Realm realm, Announcement model) {
                        model.visited = true;
                    }
                })
                .run();
    }

    @Override
    public void onRun() throws Throwable {
        if (UserManager.isAuthorized()) {
            Announcement.JsonModel model = new Announcement.JsonModel();
            model.setId(announcementId);
            model.visited = true;

            final Response<Announcement.JsonModel> response = ApiService.getInstance().updateAnnouncement(
                    UserManager.getTokenAsHeader(),
                    announcementId,
                    model
            ).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Announcement visit successfully updated");

                if (callback != null) callback.success();
            } else {
                if (Config.DEBUG) Log.e(TAG, "Error while updating announcement visit");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
        }
    }

}
