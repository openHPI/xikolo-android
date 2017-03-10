package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiService;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import moe.banana.jsonapi2.HasOne;
import retrofit2.Response;

public class CreateEnrollmentJob extends BaseJob {

    public static final String TAG = CreateEnrollmentJob.class.getSimpleName();

    private String courseId;

    public CreateEnrollmentJob(String courseId, JobCallback callback) {
        super(new Params(Priority.HIGH), callback);
        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isAuthorized()) {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline()) {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        } else {
            Enrollment.JsonModel enrollment = new Enrollment.JsonModel();
            String type = new Course.JsonModel().getType();
            enrollment.course = new HasOne<>(type, courseId);

            final Response<Enrollment.JsonModel> response = ApiService.getInstance().createEnrollment(
                    UserManager.getTokenAsHeader(),
                    enrollment
            ).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment created");

                if (callback != null) callback.onSuccess();

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(response.body().convertToRealmObject());
                    }
                });
                realm.close();
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not created");
                if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
            }
        }
    }

}
