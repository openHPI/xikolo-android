package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiService;
import de.xikolo.config.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
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
                final Response<Enrollment.JsonModel[]> response = ApiService.getInstance().listEnrollments(UserManager.getToken()).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Enrollments received (" + response.body().length + ")");

                    if (callback != null) callback.success();

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for (Enrollment.JsonModel model : response.body()) {
                                realm.copyToRealmOrUpdate(model.convertToRealmObject());
                            }
                        }
                    });
                    realm.close();
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
