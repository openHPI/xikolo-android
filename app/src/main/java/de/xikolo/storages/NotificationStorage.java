package de.xikolo.storages;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.storages.base.BaseStorage;

public class NotificationStorage extends BaseStorage {

    private static final String PREF_NOTIFICATIONS = "pref_notifications";

    private String DOWNLOAD_NOTIFICATIONS = "download_notifications";

    public NotificationStorage() {
        super(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
    }

    public List<String> getDownloadNotifications() {
        Gson gson = new Gson();
        String json = preferences.getString(DOWNLOAD_NOTIFICATIONS, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveDownloadNotifications(List<String> notifications) {
        SharedPreferences.Editor editor = preferences.edit();
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

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        editor.putString(DOWNLOAD_NOTIFICATIONS, gson.toJson(notifications));
        editor.commit();
    }

    public void deleteDownloadNotification(String notification) {
        List<String> notifications = getDownloadNotifications();

        if (notifications != null) {
            notifications.remove(notification);
            SharedPreferences.Editor editor = preferences.edit();
            Gson gson = new Gson();
            editor.putString(DOWNLOAD_NOTIFICATIONS, gson.toJson(notifications));
            editor.commit();
        }
    }

    public void deleteAllDownloadNotifications() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

}
