package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.network.ApiService;
import de.xikolo.config.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetCourseJob extends BaseJob {

    public static final String TAG = GetCourseJob.class.getSimpleName();

    private String courseId;

    public GetCourseJob(String courseId, JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            final Response<Course.JsonModel> response;

            if (UserManager.isAuthorized()) {
                response = ApiService.getInstance().getCourse(UserManager.getTokenAsHeader()).execute();
            } else {
                response = ApiService.getInstance().getCourse(courseId).execute();
            }

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Course received");

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
                if (Config.DEBUG) Log.e(TAG, "Error while fetching course");
                if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
