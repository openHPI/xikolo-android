package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveCourseJob extends Job {

    public static final String TAG = RetrieveCourseJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String courseId;

    private Result<Course> result;

    public RetrieveCourseJob(Result<Course> result, String courseId) {
        super(new Params(Priority.MID));
        this.id = jobCounter.incrementAndGet();

        this.courseId = courseId;

        this.result = result;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        CourseDataAccess courseDataAccess = GlobalApplication.getInstance()
                .getDataAccessFactory().getCourseDataAccess();
        result.success(courseDataAccess.getCourse(courseId), Result.DataSource.LOCAL);

        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            String url = Config.API + Config.COURSES + courseId;

            Response response = new ApiRequest(url).execute();
            if (response.isSuccessful()) {
                Course course = ApiParser.parse(response, Course.class);
                response.close();

                if (Config.DEBUG) Log.i(TAG, "Course received (" + course.id + ")");

                courseDataAccess.addOrUpdateCourse(course, false);

                result.success(course, Result.DataSource.NETWORK);
            } else {
                if (Config.DEBUG) Log.w(TAG, "No Courses received");
                result.error(Result.ErrorCode.NO_RESULT);
            }
        } else {
            result.warn(Result.WarnCode.NO_NETWORK);
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
