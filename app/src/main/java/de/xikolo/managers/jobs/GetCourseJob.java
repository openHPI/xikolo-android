package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.network.ApiV2Request;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetCourseJob extends Job {

    public static final String TAG = GetCourseJob.class.getSimpleName();

    private String courseId;

    public GetCourseJob(String courseId) {
        super(new Params(Priority.MID));

        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {

            final Response<Course.JsonModel> response;

            if (UserManager.isLoggedIn()) {
                response = ApiV2Request.service().getCourse(UserManager.getTokenHeader()).execute();
            } else {
                response = ApiV2Request.service().getCourse(courseId).execute();
            }

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Course received");

                EventBus.getDefault().postSticky(new GetCourseJobEvent(NetworkJobEvent.State.SUCCESS, courseId));

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
                EventBus.getDefault().postSticky(new GetCourseJobEvent(NetworkJobEvent.State.ERROR, courseId));
            }
        } else {
            EventBus.getDefault().postSticky(new GetCourseJobEvent(NetworkJobEvent.State.NO_NETWORK, courseId));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        EventBus.getDefault().postSticky(new GetCourseJobEvent(NetworkJobEvent.State.CANCEL, courseId));
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    public static class GetCourseJobEvent extends NetworkJobEvent {

        public GetCourseJobEvent(State state, String id) {
            super(state, id);
        }

    }

}
