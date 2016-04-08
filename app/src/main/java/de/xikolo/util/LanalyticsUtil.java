package de.xikolo.util;

import android.content.Context;

import de.xikolo.GlobalApplication;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.lanalytics.Tracker;
import de.xikolo.model.UserModel;

public class LanalyticsUtil {

    public static final String TAG = LanalyticsUtil.class.getSimpleName();

    public static final String VERB_VIDEO_PLAY = "VIDEO_PLAY";

    public static void track(Lanalytics.Event event) {
        Tracker tracker = GlobalApplication.getInstance()
                .getLanalytics().getDefaultTracker();

        tracker.track(event);
    }

    public static Lanalytics.Event.Builder newBuilder(Context context) {
        Lanalytics.Event.Builder builder = new Lanalytics.Event.Builder(context);

        if (UserModel.isLoggedIn(context)) {
            UserPreferences userPreferences = GlobalApplication.getInstance()
                    .getPreferencesFactory().getUserPreferences();
            builder.setUser(userPreferences.getUser().id);
        }

        return builder;
    }

}
