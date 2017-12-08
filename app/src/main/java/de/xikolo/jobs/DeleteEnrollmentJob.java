package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.models.base.Local;
import de.xikolo.models.base.Sync;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class DeleteEnrollmentJob extends BaseJob {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private String id;

    public DeleteEnrollmentJob(String id, JobCallback callback) {
        super(new Params(PRIORITY_HIGH), callback);
        this.id = id;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | id " + id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isAuthorized()) {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline()) {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        } else {
            Response response = ApiService.getInstance().deleteEnrollment(id).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment deleted");

                Local.Delete.with(Enrollment.class, id)
                        .setBeforeCommitCallback(new Sync.BeforeCommitCallback<Enrollment>() {
                            @Override
                            public void beforeCommit(Realm realm, Enrollment model) {
                                Course course = realm.where(Course.class).equalTo("enrollmentId", model.id).findFirst();
                                if (course != null) course.enrollmentId = null;
                            }
                        })
                        .run();

                if (callback != null) callback.success();
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not deleted");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        }
    }

}
