package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.DataAccessFactory;
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

    private Result<Course> result;
    private Course course;

    public DeleteEnrollmentJob(Result<Course> result, Course course) {
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
                DataAccessFactory dataAccessFactory = GlobalApplication.getInstance().getDataAccessFactory();
                dataAccessFactory.getCourseDataAccess().updateCourse(course, false);
                ModuleDataAccess moduleDataAccess = dataAccessFactory.getModuleDataAccess();
                for (Module module : moduleDataAccess.getAllModulesForCourse(course)) {
                    moduleDataAccess.deleteModule(module);
                }

                result.success(course, Result.DataSource.NETWORK);
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
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
