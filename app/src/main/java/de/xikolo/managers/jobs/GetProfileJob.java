package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.models.Profile;
import de.xikolo.network.ApiService;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetProfileJob extends BaseJob {

    public static final String TAG = GetProfileJob.class.getSimpleName();

    public GetProfileJob(JobCallback callback) {
        super(new Params(Priority.HIGH), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {
                final Response<Profile.JsonModel> response = ApiService.getInstance().getProfile(
                        UserManager.getTokenAsHeader(),
                        UserManager.getUserId()
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Profile received");

                    if (callback != null) callback.onSuccess();

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(response.body().addToRealm());
                        }
                    });
                    realm.close();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching course");
                    if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
                }
            } else {
            }
        } else {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
