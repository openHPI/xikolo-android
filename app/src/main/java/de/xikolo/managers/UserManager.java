package de.xikolo.managers;

import com.path.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.events.LogoutEvent;
import de.xikolo.managers.jobs.CreateAccessTokenJob;
import de.xikolo.managers.jobs.RetrieveUserJob;
import de.xikolo.models.User;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.storages.preferences.UserStorage;

public class UserManager extends BaseManager {

    public static final String TAG = UserManager.class.getSimpleName();

    public UserManager(JobManager jobManager) {
        super(jobManager);
    }

    public static String getToken() {
        UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
        return userStorage.getAccessToken().token;
    }

    public static User getSavedUser() {
        UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
        return userStorage.getUser();
    }

    public static boolean isLoggedIn() {
        UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
        return userStorage.getAccessToken().token != null;
    }

    public void logout() {
        GlobalApplication application = GlobalApplication.getInstance();

        UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
        userStorage.deleteUser();

        application.getDatabaseHelper().deleteDatabase();

        application.getLanalytics().deleteData();

        EventBus.getDefault().post(new LogoutEvent());
    }

    public void login(Result<Void> result, String email, String password) {
        jobManager.addJobInBackground(new CreateAccessTokenJob(result, email, password));
    }

    public void getUser(Result<User> result) {
        jobManager.addJobInBackground(new RetrieveUserJob(result));
    }

}
