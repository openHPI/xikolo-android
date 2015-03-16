package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {

    public static boolean isVideoQualityLimitedOnMobile(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("video_quality", true);
    }

    public static boolean isDownloadNetworkLimitedOnMobile(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("download_network", true);
    }

    public static boolean confirmBeforeDeleting(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("confirm_delete", true);
    }

    public static void setConfirmBeforeDeleting(Context context, boolean confirmBeforeDeleting) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("confirm_delete", confirmBeforeDeleting);
        editor.commit();
    }

}
