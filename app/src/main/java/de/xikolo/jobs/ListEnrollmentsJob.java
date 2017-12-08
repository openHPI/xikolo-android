package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListEnrollmentsJob extends BaseJob {

    public static final String TAG = ListEnrollmentsJob.class.getSimpleName();

    public ListEnrollmentsJob(JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {

                Response<Enrollment.JsonModel[]> response =
                        ApiService.getInstance().listEnrollments().execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Enrollments received");

                    Sync.Data.with(Enrollment.class, response.body());

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching enrollment list");
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
