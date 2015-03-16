package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {

    public static boolean isVideoQualityLimitedOnMobile(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("video_quality", true);
    }

    public static void setIsVideoQualityLimitedOnMobile(Context context, boolean isVideoQualityLimitedOnMobile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("video_quality", isVideoQualityLimitedOnMobile);
        editor.commit();
    }

    public static boolean isDownloadNetworkLimitedOnMobile(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("download_network", true);
    }

    public static void setIsDownloadNetworkLimitedOnMobile(Context context, boolean isDownloadNetworkLimitedOnMobile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("download_network", isDownloadNetworkLimitedOnMobile);
        editor.commit();
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
