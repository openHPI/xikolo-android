package de.xikolo.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import de.xikolo.R;
import de.xikolo.util.PlaybackSpeed;

public class AppPreferences extends Preferences {

    public AppPreferences(Context context) {
        super(context);
    }

    private boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    private boolean getBoolean(String key, boolean defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(key, defValue);
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

    public PlaybackSpeed getVideoPlaybackSpeed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String speed = preferences.getString(mContext.getString(R.string.preference_video_playback_speed),
                mContext.getString(R.string.settings_default_value_video_playback_speed));
        return PlaybackSpeed.get(speed);
    }

    public void setVideoPlaybackSpeed(PlaybackSpeed speed) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(mContext.getString(R.string.preference_video_playback_speed),
                speed.toString());
        editor.apply();
    }

    public boolean usedSecondScreen() {
        return getBoolean(mContext.getString(R.string.preference_used_second_screen), false);
    }

    public void setUsedSecondScreen(boolean used) {
        putBoolean(used, mContext.getString(R.string.preference_used_second_screen));
    }

    public boolean showedSecondScreenDialog() {
        return getBoolean(mContext.getString(R.string.preference_dialog_second_screen), false);
    }

    public void setShowedSecondScreenDialog(boolean showed) {
        putBoolean(showed, mContext.getString(R.string.preference_dialog_second_screen));
    }

}
