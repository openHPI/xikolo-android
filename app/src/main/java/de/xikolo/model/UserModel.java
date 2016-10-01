package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.User;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.model.events.LogoutEvent;
import de.xikolo.model.jobs.CreateAccessTokenJob;
import de.xikolo.model.jobs.RetrieveUserJob;

public class UserModel extends BaseModel {

    public static final String TAG = UserModel.class.getSimpleName();

    public UserModel(JobManager jobManager) {
        super(jobManager);
    }

    public static String getToken(Context context) {
        UserPreferences prefs = GlobalApplication.getInstance()
                .getPreferencesFactory().getUserPreferences();
        return prefs.getAccessToken().token;
    }

    public static User getSavedUser(Context context) {
        UserPreferences prefs = GlobalApplication.getInstance()
                .getPreferencesFactory().getUserPreferences();
        return prefs.getUser();
    }

    public static boolean isLoggedIn(Context context) {
        UserPreferences prefs = GlobalApplication.getInstance()
                .getPreferencesFactory().getUserPreferences();
        return prefs.getAccessToken().token != null;
    }

    public void logout() {
        GlobalApplication application = GlobalApplication.getInstance();

        application.getPreferencesFactory().getUserPreferences().deleteUser();
        application.getDataAccessFactory().getDatabaseHelper().deleteDatabase();
        application.getLanalytics().deleteData();

        EventBus.getDefault().post(new LogoutEvent());
    }

    public void login(Result<Void> result, String email, String password) {
        mJobManager.addJobInBackground(new CreateAccessTokenJob(result, email, password));
    }

    public void getUser(Result<User> result) {
        mJobManager.addJobInBackground(new RetrieveUserJob(result));
    }

}
