package de.xikolo.util;

import android.content.Context;
import android.util.Log;

import de.xikolo.GlobalApplication;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.lanalytics.Tracker;
import de.xikolo.model.UserModel;

public class LanalyticsUtil {

    public static final String TAG = LanalyticsUtil.class.getSimpleName();

    public static final String VERB_VIDEO_PLAY = "VIDEO_PLAY";

    public static void track(Lanalytics.Event event) {
        GlobalApplication application = GlobalApplication.getInstance();
        if (UserModel.isLoggedIn(application)) {
            Tracker tracker = application.getLanalytics().getDefaultTracker();
            tracker.track(event, UserModel.getToken(GlobalApplication.getInstance()));
        } else {
            Log.e(TAG, "Couldn't track event " + event.verb + ". No user login found.");
        }
    }

    public static Lanalytics.Event.Builder newEventBuilder() {
        GlobalApplication application = GlobalApplication.getInstance();
        Lanalytics.Event.Builder builder = new Lanalytics.Event.Builder(application);

        if (UserModel.isLoggedIn(application)) {
            UserPreferences userPreferences = application.getPreferencesFactory().getUserPreferences();
            builder.setUser(userPreferences.getUser().id);
        }

        return builder;
    }

}
