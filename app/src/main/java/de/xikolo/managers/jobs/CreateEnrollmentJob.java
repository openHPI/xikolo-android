package de.xikolo.managers.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.CourseDataAdapter;
import de.xikolo.models.Course;
import de.xikolo.network.ApiRequest;
import de.xikolo.managers.Result;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateEnrollmentJob extends Job {

    public static final String TAG = CreateEnrollmentJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Course course;
    private Result<Course> result;

    public CreateEnrollmentJob(Result<Course> result, Course course) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

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
            String url = Config.API + Config.USER + Config.ENROLLMENTS;

            RequestBody body = new FormBody.Builder()
                    .add("course_id", course.id)
                    .build();

            Response response = new ApiRequest(url)
                    .post(body)
                    .execute();

            if (response.isSuccessful()) {
                response.close();

                if (Config.DEBUG) Log.i(TAG, "Enrollment created");

                course.is_enrolled = true;
                CourseDataAdapter courseDataAccess = (CourseDataAdapter) GlobalApplication.getDataAdapter(DataType.COURSE);
                courseDataAccess.update(course, false);
                result.success(course, Result.DataSource.NETWORK);
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not created");
                result.error(Result.ErrorCode.NO_RESULT);
            }
        }

    }

    @Override
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
