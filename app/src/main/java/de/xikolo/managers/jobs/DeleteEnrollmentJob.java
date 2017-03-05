package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiV2Request;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class DeleteEnrollmentJob extends BaseJob {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private String id;

    public DeleteEnrollmentJob(String id, JobCallback callback) {
        super(new Params(Priority.HIGH), callback);
        this.id = id;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | id " + id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        } else {
            Response response = ApiV2Request.service()
                    .deleteEnrollment(UserManager.getTokenHeader(), id).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment deleted");

                if (callback != null) callback.onSuccess();

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(Enrollment.class).equalTo("id", id).findFirst().deleteFromRealm();
                    }
                });
                realm.close();
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not deleted");
                if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
            }
        }
    }

}
