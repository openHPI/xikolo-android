package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Module;
import de.xikolo.network.ApiRequest;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.CourseDataAdapter;
import de.xikolo.storages.databases.adapters.ModuleDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class DeleteEnrollmentJob extends Job {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private Course course;
    private Result<Course> result;

    public DeleteEnrollmentJob(Result<Course> result, Course course) {
        super(new Params(Priority.HIGH));

        this.result = result;
        this.course = course;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + course.id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_NETWORK);
        } else {
            String url = Config.API + Config.USER + Config.ENROLLMENTS + course.id;

            Response response = new ApiRequest(url)
                    .delete()
                    .execute();

            if (response.isSuccessful()) {
                response.close();

                if (Config.DEBUG) Log.i(TAG, "Enrollment deleted");

                course.is_enrolled = false;

                CourseDataAdapter courseDataAdapter = (CourseDataAdapter) GlobalApplication.getDataAdapter(DataType.COURSE);
                courseDataAdapter.update(course, false);

                ModuleDataAdapter moduleDataAdapter = (ModuleDataAdapter) GlobalApplication.getDataAdapter(DataType.MODULE);
                for (Module module : moduleDataAdapter.getAllForCourse(course.id)) {
                    moduleDataAdapter.delete(module.id);
                }

                result.success(course, Result.DataSource.NETWORK);
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not deleted");
                result.error(Result.ErrorCode.NO_RESULT);
            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
