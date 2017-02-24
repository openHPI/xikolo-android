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
import de.xikolo.models.Enrollment;
import de.xikolo.network.ApiV2Request;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import moe.banana.jsonapi2.HasOne;
import retrofit2.Response;

public class CreateEnrollmentJob extends Job {

    public static final String TAG = CreateEnrollmentJob.class.getSimpleName();

    private String courseId;

    public CreateEnrollmentJob(String courseId) {
        super(new Params(Priority.HIGH));

        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            EventBus.getDefault().postSticky(new CreateEnrollmentJobEvent(NetworkJobEvent.State.NO_AUTH, courseId));
        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            EventBus.getDefault().postSticky(new CreateEnrollmentJobEvent(NetworkJobEvent.State.NO_NETWORK, courseId));
        } else {
            Enrollment.JsonModel enrollment = new Enrollment.JsonModel();
            String type = new Course.JsonModel().getType();
            enrollment.course = new HasOne<>(type, courseId);

            final Response<Enrollment.JsonModel> response = ApiV2Request.service()
                    .postEnrollment(UserManager.getTokenHeader(), enrollment).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Enrollment created");

                EventBus.getDefault().postSticky(new CreateEnrollmentJobEvent(NetworkJobEvent.State.SUCCESS, courseId));

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(response.body().convertToRealmObject());
                    }
                });
                realm.close();
            } else {
                if (Config.DEBUG) Log.w(TAG, "Enrollment not created");
                EventBus.getDefault().postSticky(new CreateEnrollmentJobEvent(NetworkJobEvent.State.ERROR, courseId));
            }
        }

    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        EventBus.getDefault().postSticky(new CreateEnrollmentJobEvent(NetworkJobEvent.State.CANCEL, courseId));
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    public static class CreateEnrollmentJobEvent extends NetworkJobEvent {

        public CreateEnrollmentJobEvent(State state, String id) {
            super(state, id);
        }

    }

}
