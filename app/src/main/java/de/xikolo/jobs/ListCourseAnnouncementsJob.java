package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListCourseAnnouncementsJob extends BaseJob {

    public static final String TAG = ListCourseAnnouncementsJob.class.getSimpleName();

    private String courseId;

    public ListCourseAnnouncementsJob(String courseId, JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {

                Response<Announcement.JsonModel[]> response =
                        ApiService.getInstance().listCourseAnnouncements(courseId).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Announcements received");

                    Sync.Data.with(Announcement.class, response.body())
                            .addFilter("courseId", courseId)
                            .run();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching announcements list");
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
