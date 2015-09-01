package de.xikolo.data.preferences;

import android.content.Context;

public class PreferencesFactory {

    private AppPreferences appPreferences;

    private UserPreferences userPreferences;

    private Context context;

    public PreferencesFactory(Context context) {
        this.context = context;
    }

    public AppPreferences getAppPreferences() {
        if (appPreferences == null) {
            appPreferences = new AppPreferences(context);
        }
        return appPreferences;
    }

    public UserPreferences getUserPreferences() {
        if (userPreferences == null) {
            userPreferences = new UserPreferences(context);
        }
        return userPreferences;
    }
}
