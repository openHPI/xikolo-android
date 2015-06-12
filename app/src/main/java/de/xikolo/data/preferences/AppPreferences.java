package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.xikolo.R;

public class AppPreferences {

    private static boolean getBoolean(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, true);
    }

    private static void putBoolean(boolean value, String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean isVideoQualityLimitedOnMobile(Context context) {
        return getBoolean(context.getString(R.string.preference_video_quality), context);
    }

    public static void setIsVideoQualityLimitedOnMobile(Context context, boolean value) {
        putBoolean(value, context.getString(R.string.preference_video_quality), context);
    }

    public static boolean isDownloadNetworkLimitedOnMobile(Context context) {
        return getBoolean(context.getString(R.string.preference_download_network), context);
    }

    public static void setIsDownloadNetworkLimitedOnMobile(Context context, boolean value) {
        putBoolean(value, context.getString(R.string.preference_download_network), context);
    }

    public static boolean confirmBeforeDeleting(Context context) {
        return getBoolean(context.getString(R.string.preference_confirm_delete), context);
    }

    public static void setConfirmBeforeDeleting(Context context, boolean value) {
        putBoolean(value, context.getString(R.string.preference_confirm_delete), context);
    }

    public static boolean isUsingExternalStorage(Context context) {
        return getBoolean(context.getString(R.string.preference_storage), context);
    }

    public static void setIsUsingExternalStorage(Context context, boolean value) {
        putBoolean(value, context.getString(R.string.preference_storage), context);
    }
}
