package de.xikolo.storages;

import android.content.Context;
import android.content.SharedPreferences;

import de.xikolo.storages.base.BaseStorage;

public class UserStorage extends BaseStorage {

    private static final String PREF_USER = "pref_user";

    private static String USER_ID = "id";
    private static String ACCESS_TOKEN = "token";

    public UserStorage() {
        super(PREF_USER, Context.MODE_PRIVATE);
    }

    public String getUserId() {
        return preferences.getString(USER_ID, null);
    }

    public void saveUserId(String id) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, id);
        editor.commit();
    }

    public String getAccessToken() {
        return preferences.getString(ACCESS_TOKEN, null);
    }

    public void saveAccessToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.commit();
    }

    public void delete() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

}
