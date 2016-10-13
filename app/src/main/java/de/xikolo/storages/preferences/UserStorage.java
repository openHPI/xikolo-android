package de.xikolo.storages.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import de.xikolo.models.AccessToken;
import de.xikolo.models.User;

public class UserStorage extends KeyValueStorage {

    private static final String PREF_USER = "pref_user";

    private static String ACCESS_TOKEN_DEFAULT = null;

    private static String USER_ID = "id";
    private static String USER_FIRST_NAME = "first_name";
    private static String USER_LAST_NAME = "last_name";
    private static String USER_EMAIL = "email";
    private static String USER_ACCESS_TOKEN = "token";
    private static String USER_VISUAL_URL = "visual_url";

    UserStorage(Context context) {
        super(context, PREF_USER, Context.MODE_PRIVATE);
    }

    public User getUser() {
        User user = new User();
        user.id = preferences.getString(USER_ID, null);
        user.first_name = preferences.getString(USER_FIRST_NAME, null);
        user.last_name = preferences.getString(USER_LAST_NAME, null);
        user.email = preferences.getString(USER_EMAIL, null);
        user.user_visual = preferences.getString(USER_VISUAL_URL, null);
        return user;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, user.id);
        editor.putString(USER_FIRST_NAME, user.first_name);
        editor.putString(USER_LAST_NAME, user.last_name);
        editor.putString(USER_EMAIL, user.email);
        editor.putString(USER_VISUAL_URL, user.user_visual);
        editor.commit();
    }

    public void deleteUser() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        token.token = preferences.getString(USER_ACCESS_TOKEN, ACCESS_TOKEN_DEFAULT);
        return token;
    }

    public void saveAccessToken(AccessToken token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ACCESS_TOKEN, token.token);
        editor.commit();
    }

}
