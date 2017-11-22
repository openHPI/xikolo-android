package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class ListCoursesJob extends BaseJob {

    public static final String TAG = ListCoursesJob.class.getSimpleName();

    public ListCoursesJob(JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            final Response<Course.JsonModel[]> response;

            if (UserManager.isAuthorized()) {
                response = ApiService.getInstance().listCoursesWithEnrollments(
                        UserManager.getTokenAsHeader()
                ).execute();
            } else {
                response = ApiService.getInstance().listCourses().execute();
            }

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Courses received (" + response.body().length + ")");

                syncData(Course.class, response.body());
                syncIncluded(Enrollment.class, response.body(), new BeforeCommitCallback<Enrollment>() {
                    @Override
                    public void beforeCommit(Realm realm, Enrollment model) {
                        Course course = realm.where(Course.class).equalTo("enrollmentId", model.id).findFirst();
                        if (course != null) model.courseId = course.id;
                    }
                });

                if (callback != null) callback.success();
            } else {
                if (Config.DEBUG) Log.e(TAG, "Error while fetching courses list");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
