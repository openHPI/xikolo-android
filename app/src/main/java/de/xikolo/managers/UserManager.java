package de.xikolo.managers;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.events.LogoutEvent;
import de.xikolo.managers.jobs.CreateAccessTokenJob;
import de.xikolo.managers.jobs.GetProfileJob;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Profile;
import de.xikolo.storages.preferences.UserStorage;
import de.xikolo.utils.Config;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class UserManager extends BaseManager {

    public static final String TAG = UserManager.class.getSimpleName();

    private UserStorage userStorage;

    public UserManager() {
        this.userStorage = new UserStorage();
    }

    public static String getToken() {
        UserStorage userStorage = new UserStorage();
        return userStorage.getAccessToken();
    }

    public static String getTokenAsHeader() {
        UserStorage userStorage = new UserStorage();
        return Config.HEADER_AUTHORIZATION_PREFIX_API_V2 + userStorage.getAccessToken();
    }

    public static String getUserId() {
        UserStorage userStorage = new UserStorage();
        return userStorage.getUserId();
    }

    public static boolean isAuthorized() {
        UserStorage userStorage = new UserStorage();
        return userStorage.getAccessToken() != null;
    }

    public void login(JobCallback callback, String email, String password) {
        jobManager.addJobInBackground(new CreateAccessTokenJob(callback, email, password));
    }

    public void logout() {
        GlobalApplication application = GlobalApplication.getInstance();

        UserStorage userStorage = new UserStorage();
        userStorage.delete();

        application.getDatabaseHelper().deleteDatabase();

        application.getLanalytics().deleteData();

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.close();

        EventBus.getDefault().post(new LogoutEvent());
    }

    public void requestProfile(JobCallback callback) {
        jobManager.addJobInBackground(new GetProfileJob(callback));
    }

    public Profile getProfile(Realm realm, RealmChangeListener<Profile> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        if (!UserManager.isAuthorized()) {
            return null;
        }

        Profile profilePromise = realm
                .where(Profile.class)
                .equalTo("id", UserManager.getUserId())
                .findFirstAsync();

        profilePromise.addChangeListener(listener);

        return profilePromise;
    }

}
