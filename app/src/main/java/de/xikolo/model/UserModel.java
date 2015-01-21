package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.entities.User;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.model.jobs.CreateAccessTokenJob;
import de.xikolo.model.jobs.RetrieveUserJob;

public class UserModel extends BaseModel {

    public static final String TAG = UserModel.class.getSimpleName();

    private UserPreferences mUserPref;
    private DatabaseHelper databaseHelper;

    public UserModel(Context context, JobManager jobManager, DatabaseHelper databaseHelper) {
        super(context, jobManager);

        this.mUserPref = new UserPreferences(context);
        this.databaseHelper = databaseHelper;
    }

    public static String getToken(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getAccessToken().token;
    }

    public static User getSavedUser(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getUser();
    }

    public static boolean isLoggedIn(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getAccessToken().token != null;
    }

    public void logout() {
        mUserPref.deleteUser();
        databaseHelper.deleteDatabase();
    }

    public void login(Result<Void> result, String email, String password) {
        mJobManager.addJobInBackground(new CreateAccessTokenJob(result, email, password, mUserPref));
    }

    public void getUser(Result<User> result) {
        mJobManager.addJobInBackground(new RetrieveUserJob(result, mUserPref));
    }

}
