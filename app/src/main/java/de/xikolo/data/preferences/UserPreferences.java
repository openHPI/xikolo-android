package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import de.xikolo.data.entities.AccessToken;
import de.xikolo.data.entities.User;
import de.xikolo.util.Config;

public class UserPreferences extends Preferences {

    public static final String PREF_USER = UserPreferences.class.getName();

    public static String ACCESS_TOKEN_DEFAULT = null;

    private static String USER_ID = "id";
    private static String USER_FIRST_NAME = "first_name";
    private static String USER_LAST_NAME = "last_name";
    private static String USER_EMAIL = "email";
    private static String USER_ACCESS_TOKEN = "token";
    private static String USER_VISUAL_URL = "visual_url";

    public UserPreferences(Context context) {
        super(context);
    }

    public User getUser() {
        User user = new User();
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        user.id = sharedPref.getString(USER_ID, null);
        user.first_name = sharedPref.getString(USER_FIRST_NAME, null);
        user.last_name = sharedPref.getString(USER_LAST_NAME, null);
        user.email = sharedPref.getString(USER_EMAIL, null);
        user.user_visual = sharedPref.getString(USER_VISUAL_URL, null);
        return user;
    }

    public void saveUser(User user) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_ID, user.id);
        editor.putString(USER_FIRST_NAME, user.first_name);
        editor.putString(USER_LAST_NAME, user.last_name);
        editor.putString(USER_EMAIL, user.email);
        editor.putString(USER_VISUAL_URL, user.user_visual);
        editor.commit();
    }

    public void deleteUser() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    public AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        token.token = ACCESS_TOKEN_DEFAULT;
        if (mContext != null) {
            SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
            token.token = sharedPref.getString(USER_ACCESS_TOKEN, ACCESS_TOKEN_DEFAULT);
        }
        return token;
    }

    public void saveAccessToken(AccessToken token) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_ACCESS_TOKEN, token.token);
        editor.commit();
    }

}
