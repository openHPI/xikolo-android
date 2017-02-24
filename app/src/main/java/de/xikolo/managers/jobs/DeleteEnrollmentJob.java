package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiV2Request;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class DeleteEnrollmentJob extends Job {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private String id;

    public DeleteEnrollmentJob(String id) {
        super(new Params(Priority.HIGH));

        this.id = id;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | id " + id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {

        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {

        } else {
            Response response = ApiV2Request.service()
                    .deleteEnrollment(UserManager.getTokenHeader(), id).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment deleted");

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

            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
