package de.xikolo.managers;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.App;
import de.xikolo.config.Config;
import de.xikolo.events.LogoutEvent;
import de.xikolo.jobs.CreateAccessTokenJob;
import de.xikolo.jobs.GetUserWithProfileJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.User;
import de.xikolo.storages.UserStorage;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;

public class UserManager extends BaseManager {

    public static final String TAG = UserManager.class.getSimpleName();

    public UserManager() {
    }

    public static String getToken() {
        UserStorage userStorage = new UserStorage();
        return userStorage.getAccessToken();
    }

    public static String getTokenAsHeader() {
        UserStorage userStorage = new UserStorage();
        return Config.HEADER_AUTH_VALUE_PREFIX_JSON_API + userStorage.getAccessToken();
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

    public static void logout() {
        App application = App.getInstance();

        UserStorage userStorage = new UserStorage();
        userStorage.delete();

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

    public void requestUserWithProfile(JobCallback callback) {
        jobManager.addJobInBackground(new GetUserWithProfileJob(callback));
    }

    public RealmObject getUser(Realm realm, RealmChangeListener<User> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        if (!UserManager.isAuthorized()) {
            return null;
        }

        RealmObject userPromise = realm
                .where(User.class)
                .equalTo("id", UserManager.getUserId())
                .findFirstAsync();

        userPromise.addChangeListener(listener);

        return userPromise;
    }

}
