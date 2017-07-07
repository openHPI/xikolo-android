package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Profile;
import de.xikolo.models.User;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
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
                final Response<User.JsonModel> response = ApiService.getInstance().getUserWithProfile(
                        UserManager.getTokenAsHeader()
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "User received");

                    if (callback != null) callback.success();

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            User.JsonModel model = response.body();
                            realm.copyToRealmOrUpdate(model.convertToRealmObject());
                            if (model.profile != null && model.profile.get(model.getContext()) != null) {
                                Profile p = model.profile.get(model.getContext()).convertToRealmObject();
                                realm.copyToRealmOrUpdate(p);
                            }
                        }
                    });
                    realm.close();
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
