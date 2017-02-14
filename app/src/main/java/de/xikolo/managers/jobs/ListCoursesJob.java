package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.models.Course;
import de.xikolo.network.ApiV2Request;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class ListCoursesJob extends Job {

    public static final String TAG = ListCoursesJob.class.getSimpleName();

    public ListCoursesJob() {
        super(new Params(Priority.MID));
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {

            final Response<Course.JsonModel[]> response = ApiV2Request.service().listCourses().execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Courses received (" + response.body().length + ")");

                EventBus.getDefault().postSticky(new ListCoursesJobEvent(NetworkJobEvent.State.SUCCESS, null, null));

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (Course.JsonModel model : response.body()) {
                            realm.copyToRealmOrUpdate(model.convertToRealmObject());
                        }
                    }
                });
                realm.close();
            } else {
                if (Config.DEBUG) Log.e(TAG, "Error while fetching courses list");
                EventBus.getDefault().postSticky(new ListCoursesJobEvent(NetworkJobEvent.State.ERROR, null, null));
            }
        } else {
            EventBus.getDefault().postSticky(new ListCoursesJobEvent(NetworkJobEvent.State.ERROR, NetworkJobEvent.Code.NO_NETWORK, null));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        EventBus.getDefault().postSticky(new ListCoursesJobEvent(NetworkJobEvent.State.ERROR, NetworkJobEvent.Code.CANCELED, null));
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    public static class ListCoursesJobEvent extends NetworkJobEvent {

        public ListCoursesJobEvent(State state, Code code, String id) {
            super(state, code, id);
        }

    }

}
