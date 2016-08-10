package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

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
            Type type = new TypeToken<Course>(){}.getType();

            String url = Config.API + Config.COURSES + courseId;

            JsonRequest request = new JsonRequest(url, type);
            request.setCache(false);

            if (UserModel.isLoggedIn(GlobalApplication.getInstance())) {
                String token = UserModel.getToken(GlobalApplication.getInstance());
                request.setToken(token);
            }

            Object o = request.getResponse();
            if (o != null) {
                @SuppressWarnings("unchecked")
                Course course = (Course) o;

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
