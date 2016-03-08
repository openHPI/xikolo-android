package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.AccessToken;
import de.xikolo.data.entities.User;
import de.xikolo.util.Config;

public class NotificationPreferences extends Preferences {

    public static final String PREF_NOTIFICATIONS = NotificationPreferences.class.getName();

    private String DOWNLOAD_NOTIFICATIONS;

    public NotificationPreferences(Context context) {
        super(context);
    }

    public List<String> getDownloadNotifications() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString(DOWNLOAD_NOTIFICATIONS, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> notifications = gson.fromJson(json, type);
        return notifications;
    }

    public void saveDownloadNotifications(List<String> notifications) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        editor.putString(DOWNLOAD_NOTIFICATIONS, gson.toJson(notifications));
        editor.commit();
    }

    public void addDownloadNotification(String notification) {
        List<String> notifications = getDownloadNotifications();

        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        notifications.add(notification);

        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        editor.putString(DOWNLOAD_NOTIFICATIONS, gson.toJson(notifications));
        editor.commit();
    }

    public void deleteDownloadNotification(String notification) {
        List<String> notifications = getDownloadNotifications();

        if (notifications != null) {
            notifications.remove(notification);
            SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Gson gson = new Gson();
            editor.putString(DOWNLOAD_NOTIFICATIONS, gson.toJson(notifications));
            editor.commit();
        }
    }

    public void deleteAllDownloadNotifications() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

}
