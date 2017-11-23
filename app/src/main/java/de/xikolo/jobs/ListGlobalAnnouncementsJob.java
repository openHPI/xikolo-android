package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.jobs.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListGlobalAnnouncementsJob extends BaseJob {

    public static final String TAG = ListGlobalAnnouncementsJob.class.getSimpleName();

    public ListGlobalAnnouncementsJob(JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            final Response<Announcement.JsonModel[]> response;

            if (UserManager.isAuthorized()) {
                response = ApiService.getInstance().listGlobalAnnouncementsWithCourses(
                        UserManager.getTokenAsHeader()
                ).execute();
            } else {
                response = ApiService.getInstance().listGlobalAnnouncements().execute();
            }

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Announcements received (" + response.body().length + ")");

                Sync.Data.with(Announcement.class, response.body()).run();

                if (callback != null) callback.success();
            } else {
                if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
