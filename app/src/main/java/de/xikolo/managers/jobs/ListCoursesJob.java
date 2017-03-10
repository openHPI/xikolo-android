package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.network.ApiService;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class ListCoursesJob extends BaseJob {

    public static final String TAG = ListCoursesJob.class.getSimpleName();

    public ListCoursesJob(JobCallback callback) {
        super(new Params(Priority.MID), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            final Response<Course.JsonModel[]> response;

            if (UserManager.isAuthorized()) {
                response = ApiService.getInstance().listCoursesWithEnrollments(UserManager.getTokenAsHeader()).execute();
            } else {
                response = ApiService.getInstance().listCourses().execute();
            }

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Courses received (" + response.body().length + ")");

                if (callback != null) callback.onSuccess();

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
                if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
