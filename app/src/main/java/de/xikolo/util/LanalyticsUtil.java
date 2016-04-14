package de.xikolo.util;

import android.content.res.Configuration;
import android.util.Log;

import de.xikolo.GlobalApplication;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.lanalytics.Tracker;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.UserModel;

public class LanalyticsUtil {

    public static final String TAG = LanalyticsUtil.class.getSimpleName();

    public static final String VERB_VIDEO_PLAY = "VIDEO_PLAY";
    public static final String VERB_VIDEO_PAUSE = "VIDEO_PAUSE";
    public static final String VERB_VIDEO_SEEK = "VIDEO_SEEK";
    public static final String VERB_VIDEO_CHANGE_SPEED = "VIDEO_CHANGE_SPEED";
    public static final String VERB_DOWNLOADED_HD_VIDEO = "DOWNLOADED_HD_VIDEO";
    public static final String VERB_DOWNLOADED_SD_VIDEO = "DOWNLOADED_SD_VIDEO";
    public static final String VERB_DOWNLOADED_SLIDES = "DOWNLOADED_SLIDES";
    public static final String VERB_DOWNLOADED_TRANSCRIPT = "DOWNLOADED_TRANSCRIPT";

    public static final String VERB_VIDEO_START_CAST = "VIDEO_START_CAST";
    public static final String VERB_VIDEO_CHANGE_LANDSCAPE = "VIDEO_LANDSCAPE";
    public static final String VERB_VIDEO_CHANGE_PORTRAIT = "VIDEO_PORTRAIT";
    public static final String VERB_VIDEO_CHANGE_QUALITY = "VIDEO_CHANGE_QUALITY";
    public static final String VERB_DOWNLOADED_SECTION = "DOWNLOADED_SECTION";

    public static final String CONTEXT_COURSE_ID = "course_id";
    public static final String CONTEXT_CURRENT_TIME = "current_time";
    public static final String CONTEXT_CURRENT_SPEED = "current_speed";
    public static final String CONTEXT_OLD_CURRENT_TIME = "old_current_time";
    public static final String CONTEXT_NEW_CURRENT_TIME = "new_current_time";
    public static final String CONTEXT_OLD_SPEED = "old_speed";
    public static final String CONTEXT_NEW_SPEED = "new_speed";
    public static final String CONTEXT_HD_VIDEO = "hd_video";

    public static final String CONTEXT_SECTION_ID = "section_id";
    public static final String CONTEXT_SD_VIDEO = "sd_video";
    public static final String CONTEXT_SLIDES = "slides";
    public static final String CONTEXT_CURRENT_ORIENTATION = "current_orientation";
    public static final String CONTEXT_LANDSCAPE = "landscape";
    public static final String CONTEXT_PORTRAIT = "portrait";
    public static final String CONTEXT_QUALITY = "current_quality";
    public static final String CONTEXT_SOURCE = "current_source";
    public static final String CONTEXT_OLD_QUALITY = "old_quality";
    public static final String CONTEXT_NEW_QUALITY = "new_quality";
    public static final String CONTEXT_OLD_SOURCE = "old_source";
    public static final String CONTEXT_NEW_SOURCE = "new_source";
    public static final String CONTEXT_ONLINE = "online";
    public static final String CONTEXT_OFFLINE = "offline";


    public static void trackVideoPlay(String resourceId, String courseId, String sectionId,
                                      int currentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_PLAY)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_CURRENT_ORIENTATION,
                        currentOrientation == Configuration.ORIENTATION_PORTRAIT ? CONTEXT_PORTRAIT : CONTEXT_LANDSCAPE)
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackVideoPause(String resourceId, String courseId, String sectionId,
                                       int currentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_PAUSE)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_CURRENT_ORIENTATION,
                        currentOrientation == Configuration.ORIENTATION_PORTRAIT ? CONTEXT_PORTRAIT : CONTEXT_LANDSCAPE)
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackVideoChangeSpeed(String resourceId, String courseId, String sectionId,
                                             int currentTime, Float oldSpeed, Float newSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_CHANGE_SPEED)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_OLD_SPEED, oldSpeed.toString())
                .putContext(CONTEXT_NEW_SPEED, newSpeed.toString())
                .putContext(CONTEXT_CURRENT_ORIENTATION,
                        currentOrientation == Configuration.ORIENTATION_PORTRAIT ? CONTEXT_PORTRAIT : CONTEXT_LANDSCAPE)
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackVideoSeek(String resourceId, String courseId, String sectionId,
                                      int oldCurrentTime, int newCurrentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_SEEK)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_OLD_CURRENT_TIME, formatTime(oldCurrentTime))
                .putContext(CONTEXT_NEW_CURRENT_TIME, formatTime(newCurrentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_CURRENT_ORIENTATION,
                        currentOrientation == Configuration.ORIENTATION_PORTRAIT ? CONTEXT_PORTRAIT : CONTEXT_LANDSCAPE)
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackDownloadedFile(String resourceId, String courseId, String sectionId, DownloadModel.DownloadFileType type) {
        String verb = null;
        switch (type) {
            case VIDEO_HD: verb = VERB_DOWNLOADED_HD_VIDEO; break;
            case VIDEO_SD: verb = VERB_DOWNLOADED_SD_VIDEO; break;
            case SLIDES: verb = VERB_DOWNLOADED_SLIDES; break;
            case TRANSCRIPT: verb = VERB_DOWNLOADED_TRANSCRIPT; break;
        }

        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(verb)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build());
    }

    public static void trackVideoStartCast(String resourceId, String courseId, String sectionId, int currentTime) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_START_CAST)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .build());
    }

    public static void trackVideoChangeOrientation(String resourceId, String courseId, String sectionId,
                                                   int currentTime, Float currentSpeed, int newOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(newOrientation == Configuration.ORIENTATION_PORTRAIT ? VERB_VIDEO_CHANGE_PORTRAIT : VERB_VIDEO_CHANGE_LANDSCAPE)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackVideoChangeQuality(String resourceId, String courseId, String sectionId,
                                               int currentTime, Float currentSpeed, int currentOrientation, String oldQuality, String newQuality, String oldSource, String newSource) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_VIDEO_CHANGE_QUALITY)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_CURRENT_ORIENTATION,
                        currentOrientation == Configuration.ORIENTATION_PORTRAIT ? CONTEXT_PORTRAIT : CONTEXT_LANDSCAPE)
                .putContext(CONTEXT_OLD_QUALITY, oldQuality)
                .putContext(CONTEXT_NEW_QUALITY, newQuality)
                .putContext(CONTEXT_OLD_SOURCE, oldSource)
                .putContext(CONTEXT_NEW_SOURCE, newSource)
                .build());
    }

    public static void trackDownloadedSection(String resourceId, String courseId, Boolean hdVideo, Boolean sdVideo, Boolean slides) {
        track(newEventBuilder()
                .setResource(resourceId)
                .setVerb(VERB_DOWNLOADED_SECTION)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_HD_VIDEO, hdVideo.toString())
                .putContext(CONTEXT_SD_VIDEO, sdVideo.toString())
                .putContext(CONTEXT_SLIDES, slides.toString())
                .build());
    }

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

    private static String formatTime(int millis) {
        return String.valueOf(millis / 1000.);
    }

}
