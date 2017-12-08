package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Profile;
import de.xikolo.models.User;
import de.xikolo.models.base.Sync;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class GetUserWithProfileJob extends BaseJob {

    public static final String TAG = GetUserWithProfileJob.class.getSimpleName();

    public GetUserWithProfileJob(JobCallback callback) {
        super(new Params(PRIORITY_HIGH), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {

                Response<User.JsonModel> response =
                        ApiService.getInstance().getUserWithProfile().execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "User received");

                    Sync.Data.with(User.class, response.body()).run();
                    Sync.Included.with(Profile.class, response.body()).run();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching course");
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
