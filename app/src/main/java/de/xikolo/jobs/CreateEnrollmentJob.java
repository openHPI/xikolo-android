package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import moe.banana.jsonapi2.HasOne;
import retrofit2.Response;

public class CreateEnrollmentJob extends BaseJob {

    public static final String TAG = CreateEnrollmentJob.class.getSimpleName();

    private String courseId;

    public CreateEnrollmentJob(String courseId, JobCallback callback) {
        super(new Params(PRIORITY_HIGH), callback);
        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isAuthorized()) {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline()) {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        } else {
            Enrollment.JsonModel enrollment = new Enrollment.JsonModel();
            enrollment.course = new HasOne<>(new Course.JsonModel().getType(), courseId);

            final Response<Enrollment.JsonModel> response = ApiService.getInstance().createEnrollment(
                    UserManager.getTokenAsHeader(),
                    enrollment
            ).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment created");

                Sync.Data.with(Enrollment.class, response.body())
                        .saveOnly()
                        .setBeforeCommitCallback(new Sync.BeforeCommitCallback<Enrollment>() {
                            @Override
                            public void beforeCommit(Realm realm, Enrollment model) {
                                Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                                if (course != null) course.enrollmentId = model.id;
                            }
                        })
                        .run();

                if (callback != null) callback.success();
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not created");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        }
    }

}
