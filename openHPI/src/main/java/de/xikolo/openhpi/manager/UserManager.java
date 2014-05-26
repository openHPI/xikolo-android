package de.xikolo.openhpi.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.dataaccess.UserPreferences;
import de.xikolo.openhpi.model.AccessToken;
import de.xikolo.openhpi.model.User;
import de.xikolo.openhpi.util.Config;

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
        if (Config.DEBUG)
            Log.i(TAG, "requestUser() called");

        Type type = new TypeToken<User>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_USER, type, mContext) {
            @Override
            public void onJsonRequestReceived(Object o) {
                if (o != null) {
                    User user = (User) o;
                    Log.i(TAG, "User received: " + user.name);
                    mUserPref.saveUser(user);
                    onUserRequestReceived(user);
                } else {
                    if (Config.DEBUG)
                        Log.w(TAG, "No User received");
                    onUserRequestCancelled();
                }
            }

            @Override
            public void onJsonRequestCancelled() {
                if (Config.DEBUG)
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
