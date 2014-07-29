package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.BuildConfig;
import de.xikolo.dataaccess.EnrollmentsPreferences;
import de.xikolo.dataaccess.JsonRequest;
import de.xikolo.dataaccess.UserPreferences;
import de.xikolo.model.AccessToken;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;

public abstract class TokenManager {

    public static final String TAG = TokenManager.class.getSimpleName();

    private Context mContext;
    private UserPreferences mUserPref;
    private EnrollmentsPreferences mEnrollPref;

    public TokenManager(Context context) {
        super();
        this.mContext = context;
        this.mUserPref = new UserPreferences(context);
        this.mEnrollPref = new EnrollmentsPreferences(context);
    }

    public static String getAccessToken(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getAccessToken().access_token;
    }

    public static boolean isLoggedIn(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return !prefs.getAccessToken().access_token
                .equals(UserPreferences.ACCESS_TOKEN_DEFAULT);
    }

    public void logout() {
        this.mUserPref.deleteUser();
        this.mEnrollPref.deleteEnrollmentsSize();
    }

    public void login(String email, String password) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "login() called");

        Type type = new TypeToken<AccessToken>() {
        }.getType();

        String query = "?email=" + email + "&password=" + password;

        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_AUTHENTICATE + query, type, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (o != null) {
                    AccessToken token = (AccessToken) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "Access Token received: " + token.access_token);
                    mUserPref.saveAccessToken(token);
                    onAccessTokenRequestReceived(token);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No Access Token received");
                    onAccessTokenRequestCancelled();
                }
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "User Access Token cancelled");
                onAccessTokenRequestCancelled();
            }
        };
        request.setCache(false);
        request.setToken(getAccessToken(mContext));
        request.setMethod(Config.HTTP_POST);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onAccessTokenRequestReceived(AccessToken token);

    public abstract void onAccessTokenRequestCancelled();

}
