package de.xikolo.openhpi.dataaccess;

import android.content.Context;
import android.content.SharedPreferences;

import de.xikolo.openhpi.model.AccessToken;
import de.xikolo.openhpi.model.User;
import de.xikolo.openhpi.util.Config;

public class UserPreferences {

    private static String USER_ID = "id";
    private static String USER_NAME = "name";
    private static String USER_SHORT_NAME = "short_name";
    private static String USER_EMAIL = "email";
    private static String USER_BIRTHDATE = "birthdate";
    private static String USER_GENDER = "gender";
    private static String USER_TIMEZONE = "time_zone";
    private static String USER_COMPANY = "company";

    private static String USER_ACCESS_TOKEN = "access_token";

    public static String ACCESS_TOKEN_DEFAULT = "fail";

    private Context mContext;

    public UserPreferences(Context context) {
        super();
        this.mContext = context;
    }

    public User getUser() {
        User user = new User();
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_USER, Context.MODE_PRIVATE);
        user.id = sharedPref.getInt(USER_ID, -1);
        user.name = sharedPref.getString(USER_NAME, null);
        user.short_name = sharedPref.getString(USER_SHORT_NAME, null);
        user.email = sharedPref.getString(USER_EMAIL, null);
        user.birthdate = sharedPref.getString(USER_BIRTHDATE, null);
        user.gender = sharedPref.getString(USER_GENDER, null);
        user.time_zone = sharedPref.getString(USER_TIMEZONE, null);
        user.company = sharedPref.getString(USER_COMPANY, null);
        return user;
    }

    public void saveUser(User user) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(USER_ID, user.id);
        editor.putString(USER_NAME, user.name);
        editor.putString(USER_SHORT_NAME, user.short_name);
        editor.putString(USER_EMAIL, user.email);
        editor.putString(USER_BIRTHDATE, user.birthdate);
        editor.putString(USER_GENDER, user.gender);
        editor.putString(USER_TIMEZONE, user.time_zone);
        editor.putString(USER_COMPANY, user.company);
        editor.commit();
    }

    public void deleteUserAndToken() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    public AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_USER, Context.MODE_PRIVATE);
        token.access_token = sharedPref.getString(USER_ACCESS_TOKEN, ACCESS_TOKEN_DEFAULT);
        return token;
    }

    public void saveUser(AccessToken token) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_ACCESS_TOKEN, token.access_token);
        editor.commit();
    }

}
