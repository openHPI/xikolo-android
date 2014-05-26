package de.xikolo.openhpi.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.dataaccess.UserPreferences;
import de.xikolo.openhpi.model.AccessToken;
import de.xikolo.openhpi.util.Config;

public abstract class AccessTokenManager {

    public static final String TAG = AccessTokenManager.class.getSimpleName();

    private Context mContext;
    private UserPreferences mUserPref;

    public AccessTokenManager(Context context) {
        super();
        this.mContext = context;
        this.mUserPref = new UserPreferences(context);
    }

    public static AccessToken getAccessToken(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getAccessToken();
    }

    public static boolean isLoggedIn(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return !prefs.getAccessToken().access_token
                .equals(UserPreferences.ACCESS_TOKEN_DEFAULT);
    }

    public void logout() {
        this.mUserPref.deleteUserAndToken();
    }

    public void login(String email, String password) {
        if (Config.DEBUG)
            Log.i(TAG, "login() called");

        Type type = new TypeToken<AccessToken>() {
        }.getType();

        String query = "?email=" + email + "&password=" + password;

        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_AUTHENTICATE + query, type, mContext) {
            @Override
            public void onJsonRequestReceived(Object o) {
                if (o != null) {
                    AccessToken token = (AccessToken) o;
                    if (Config.DEBUG)
                        Log.i(TAG, "Access Token received: " + token.access_token);
                    else
                        Log.i(TAG, "Access Token received");
                    mUserPref.saveUser(token);
                    onAccessTokenRequestReceived(token);
                } else {
                    if (Config.DEBUG)
                        Log.w(TAG, "No Access Token received");
                    onAccessTokenRequestCancelled();
                }
            }

            @Override
            public void onJsonRequestCancelled() {
                if (Config.DEBUG)
                    Log.w(TAG, "User Access Token cancelled");
                onAccessTokenRequestCancelled();
            }
        };
        request.setCache(false);
        request.setToken(mUserPref.getAccessToken().access_token);
        request.setMethod(Config.HTTP_POST);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onAccessTokenRequestReceived(AccessToken token);

    public abstract void onAccessTokenRequestCancelled();

}
