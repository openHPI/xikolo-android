package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class DeleteEnrollmentJob extends Job {

    public static final String TAG = DeleteEnrollmentJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Void> result;
    private Course course;
    private CourseDataAccess courseDataAccess;
    private ModuleDataAccess moduleDataAccess;

    public DeleteEnrollmentJob(Result<Void> result, Course course, CourseDataAccess courseDataAccess, ModuleDataAccess moduleDataAccess) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.course = course;
        this.courseDataAccess = courseDataAccess;
        this.moduleDataAccess = moduleDataAccess;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + course.id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_NETWORK);
        } else {
            String url = Config.API + Config.USER + Config.ENROLLMENTS + course.id;

            HttpRequest request = new HttpRequest(url);
            request.setMethod(Config.HTTP_DELETE);
            request.setToken(UserModel.getToken(GlobalApplication.getInstance()));
            request.setCache(false);

            Object o = request.getResponse();
            if (o != null) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment deleted");

                course.is_enrolled = false;
                courseDataAccess.updateCourse(course);
                for (Module module : moduleDataAccess.getAllModulesForCourse(course)) {
                    moduleDataAccess.deleteModule(module);
                }

                result.success(null, Result.DataSource.NETWORK);
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not deleted");
                result.error(Result.ErrorCode.NO_RESULT);
            }
        }
    }

    @Override
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
