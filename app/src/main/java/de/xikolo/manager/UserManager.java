package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.BuildConfig;
import de.xikolo.dataaccess.JsonRequest;
import de.xikolo.dataaccess.UserPreferences;
import de.xikolo.model.User;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;

public abstract class UserManager {

    public static final String TAG = UserManager.class.getSimpleName();

    private Context mContext;
    private UserPreferences mUserPref;

    public UserManager(Context context) {
        super();
        this.mContext = context;
        this.mUserPref = new UserPreferences(context);
    }

    public static User getUser(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getUser();
    }

    public void requestUser() {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestUser() called");

        Type type = new TypeToken<User>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_USER, type, mContext) {
            @Override
            public void onJsonRequestReceived(Object o) {
                if (o != null) {
                    User user = (User) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "User received: " + user.name);
                    mUserPref.saveUser(user);
                    onUserRequestReceived(user);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No User received");
                    onUserRequestCancelled();
                }
            }

            @Override
            public void onJsonRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "User Request cancelled");
                onUserRequestCancelled();
            }
        };
        request.setCache(false);
        request.setToken(mUserPref.getAccessToken().access_token);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onUserRequestReceived(User user);

    public abstract void onUserRequestCancelled();

}
