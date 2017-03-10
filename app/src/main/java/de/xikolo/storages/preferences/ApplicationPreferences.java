package de.xikolo.storages.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.utils.PlaybackSpeed;

public class ApplicationPreferences {

    private SharedPreferences preferences;

    private Context context;

    public ApplicationPreferences() {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(GlobalApplication.getInstance());
        this.context = GlobalApplication.getInstance();
    }

    private boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    private boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    private void putBoolean(boolean value, String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean isVideoQualityLimitedOnMobile() {
        return getBoolean(context.getString(R.string.preference_video_quality));
    }

    public void setIsVideoQualityLimitedOnMobile(boolean value) {
        putBoolean(value, context.getString(R.string.preference_video_quality));
    }

    public boolean isDownloadNetworkLimitedOnMobile() {
        return getBoolean(context.getString(R.string.preference_download_network));
    }

    public void setIsDownloadNetworkLimitedOnMobile(boolean value) {
        putBoolean(value, context.getString(R.string.preference_download_network));
    }

    public boolean confirmBeforeDeleting() {
        return getBoolean(context.getString(R.string.preference_confirm_delete));
    }

    public void setConfirmBeforeDeleting(boolean value) {
        putBoolean(value, context.getString(R.string.preference_confirm_delete));
    }

    public PlaybackSpeed getVideoPlaybackSpeed() {
        String speed = preferences.getString(context.getString(R.string.preference_video_playback_speed),
                context.getString(R.string.settings_default_value_video_playback_speed));
        return PlaybackSpeed.get(speed);
    }

    public void setVideoPlaybackSpeed(PlaybackSpeed speed) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.preference_video_playback_speed),
                speed.toString());
        editor.apply();
    }

    public boolean usedSecondScreen() {
        return getBoolean(context.getString(R.string.preference_used_second_screen), false);
    }

    public void setUsedSecondScreen(boolean used) {
        putBoolean(used, context.getString(R.string.preference_used_second_screen));
    }

}
