package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.models.Course;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.CourseDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveCourseJob extends Job {

    public static final String TAG = RetrieveCourseJob.class.getSimpleName();

    private String courseId;
    private Result<Course> result;

    public RetrieveCourseJob(Result<Course> result, String courseId) {
        super(new Params(Priority.MID));

        this.courseId = courseId;
        this.result = result;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        CourseDataAdapter courseDataAdapter = (CourseDataAdapter) GlobalApplication.getDataAdapter(DataType.COURSE);
        result.success(courseDataAdapter.get(courseId), Result.DataSource.LOCAL);

        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            String url = Config.API + Config.COURSES + courseId;

            Response response = new ApiRequest(url).execute();
            if (response.isSuccessful()) {
                Course course = ApiParser.parse(response, Course.class);
                response.close();

                if (Config.DEBUG) Log.i(TAG, "Course received (" + course.id + ")");

                courseDataAdapter.addOrUpdate(course, false);

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
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
