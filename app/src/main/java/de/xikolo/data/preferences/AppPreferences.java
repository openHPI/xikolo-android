package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import de.xikolo.R;

public class AppPreferences extends Preferences {

    public AppPreferences(Context context) {
        super(context);
    }

    private boolean getBoolean(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(key, true);
    }

    private void putBoolean(boolean value, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean isVideoQualityLimitedOnMobile() {
        return getBoolean(mContext.getString(R.string.preference_video_quality));
    }

    public void setIsVideoQualityLimitedOnMobile(boolean value) {
        putBoolean(value, mContext.getString(R.string.preference_video_quality));
    }

    public boolean isDownloadNetworkLimitedOnMobile() {
        return getBoolean(mContext.getString(R.string.preference_download_network));
    }

    public void setIsDownloadNetworkLimitedOnMobile(boolean value) {
        putBoolean(value, mContext.getString(R.string.preference_download_network));
    }

    public boolean confirmBeforeDeleting() {
        return getBoolean(mContext.getString(R.string.preference_confirm_delete));
    }

    public void setConfirmBeforeDeleting(boolean value) {
        putBoolean(value, mContext.getString(R.string.preference_confirm_delete));
    }

    public boolean isUsingExternalStorage() {
        return getBoolean(mContext.getString(R.string.preference_storage));
    }

    public void setIsUsingExternalStorage(boolean value) {
        putBoolean(value, mContext.getString(R.string.preference_storage));
    }
}
