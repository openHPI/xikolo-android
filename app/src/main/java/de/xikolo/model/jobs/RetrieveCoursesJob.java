package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveCoursesJob extends Job {

    public static final String TAG = RetrieveCoursesJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Course>> result;

    private boolean includeProgress;

    public RetrieveCoursesJob(Result<List<Course>> result, boolean includeProgress) {
        super(new Params(includeProgress ? Priority.MID : Priority.MID));
        this.id = jobCounter.incrementAndGet();

        this.result = result;
        this.includeProgress = includeProgress;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | includeProgress " + includeProgress);
    }

    @Override
    public void onRun() throws Throwable {
        if (includeProgress && !UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            CourseDataAccess courseDataAccess = GlobalApplication.getInstance()
                    .getDataAccessFactory().getCourseDataAccess();
            result.success(courseDataAccess.getAllCourses(), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + "?include_progress=" + includeProgress;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<List<Course>>(){}.getType();
                    List<Course> courses = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Courses received (" + courses.size() + ")");

                    for (Course course : courses) {
                        courseDataAccess.addOrUpdateCourse(course, includeProgress);
                    }

                    result.success(courses, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No Courses received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                result.warn(Result.WarnCode.NO_NETWORK);
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
