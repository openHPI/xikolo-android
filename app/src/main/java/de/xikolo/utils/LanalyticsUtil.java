package de.xikolo.utils;

import android.content.res.Configuration;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

import de.xikolo.BuildConfig;
import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.storages.preferences.UserStorage;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.lanalytics.Tracker;
import de.xikolo.managers.DownloadManager;

public class LanalyticsUtil {

    public static final String TAG = LanalyticsUtil.class.getSimpleName();

    public static final String CONTEXT_CLIENT_ID = "client_id";

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
    public static final String CONTEXT_CAST = "cast";

    public static void trackVideoPlay(String videoId, String courseId, String sectionId,
                                      int currentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VIDEO_PLAY")
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

    public static void trackVideoPause(String videoId, String courseId, String sectionId,
                                       int currentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VIDEO_PAUSE")
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

    public static void trackVideoChangeSpeed(String videoId, String courseId, String sectionId,
                                             int currentTime, Float oldSpeed, Float newSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VIDEO_CHANGE_SPEED")
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

    public static void trackVideoSeek(String videoId, String courseId, String sectionId,
                                      int oldCurrentTime, int newCurrentTime, Float currentSpeed, int currentOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VIDEO_SEEK")
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

    public static void trackDownloadedFile(String videoId, String courseId, String sectionId, DownloadManager.DownloadFileType type) {
        String verb = null;
        switch (type) {
            case VIDEO_HD: verb = "DOWNLOADED_HD_VIDEO"; break;
            case VIDEO_SD: verb = "DOWNLOADED_SD_VIDEO"; break;
            case SLIDES: verb = "DOWNLOADED_SLIDES"; break;
            case TRANSCRIPT: verb = "DOWNLOADED_TRANSCRIPT"; break;
        }

        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb(verb)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build());
    }

    public static void trackVideoChangeOrientation(String videoId, String courseId, String sectionId,
                                                   int currentTime, Float currentSpeed, int newOrientation, String quality, String source) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb(newOrientation == Configuration.ORIENTATION_PORTRAIT ? "VIDEO_PORTRAIT" : "VIDEO_LANDSCAPE")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime))
                .putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString())
                .putContext(CONTEXT_QUALITY, quality)
                .putContext(CONTEXT_SOURCE, source)
                .build());
    }

    public static void trackVideoChangeQuality(String videoId, String courseId, String sectionId,
                                               int currentTime, Float currentSpeed, int currentOrientation, String oldQuality, String newQuality, String oldSource, String newSource) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VIDEO_CHANGE_QUALITY")
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

    public static void trackDownloadedSection(String sectionId, String courseId, Boolean hdVideo, Boolean sdVideo, Boolean slides) {
        track(newEventBuilder()
                .setResource(sectionId, "section")
                .setVerb("DOWNLOADED_SECTION")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_HD_VIDEO, hdVideo.toString())
                .putContext(CONTEXT_SD_VIDEO, sdVideo.toString())
                .putContext(CONTEXT_SLIDES, slides.toString())
                .build());
    }

    public static void trackVisitedItem(String itemId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(itemId, "item")
                .setVerb("VISITED_ITEM")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedPinboard(String courseId) {
        track(newEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_PINBOARD")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedProgress(String courseId) {
        track(newEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_PROGRESS")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedLearningRooms(String courseId) {
        track(newEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_LEARNING_ROOMS")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedAnnouncements(String courseId) {
        track(newEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_ANNOUNCEMENTS")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedRecap(String courseId) {
        track(newEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_RECAP")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedProfile(String userId) {
        track(newEventBuilder()
                .setResource(userId, "user")
                .setVerb("VISITED_PROFILE")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedPreferences(String userId) {
        track(newEventBuilder()
                .setResource(userId, "user")
                .setVerb("VISITED_PREFERENCES")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedDownloads(String userId) {
        track(newEventBuilder()
                .setResource(userId, "user")
                .setVerb("VISITED_DOWNLOADS")
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenSlidesStart(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_SLIDES_START")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenSlidesStop(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_SLIDES_STOP")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenTranscriptStart(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_TRANSCRIPT_START")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenTranscriptStop(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_TRANSCRIPT_STOP")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenPinboardStart(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_PINBOARD_START")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenPinboardStop(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_PINBOARD_STOP")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenQuizStart(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_QUIZ_START")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackSecondScreenQuizStop(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("SECOND_SCREEN_QUIZ_STOP")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedSecondScreenSlides(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VISITED_SECOND_SCREEN_SLIDES")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build());
    }

    public static void trackVisitedSecondScreenTranscript(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VISITED_SECOND_SCREEN_TRANSCRIPT")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build());
    }

    public static void trackVisitedSecondScreenQuiz(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VISITED_SECOND_SCREEN_QUIZ")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build());
    }

    public static void trackVisitedSecondScreenPinboard(String videoId, String courseId, String sectionId) {
        track(newEventBuilder()
                .setResource(videoId, "video")
                .setVerb("VISITED_SECOND_SCREEN_PINBOARD")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build());
    }


    public static void track(Lanalytics.Event event) {
        GlobalApplication application = GlobalApplication.getInstance();
        if (UserManager.isLoggedIn() && isTrackingEnabled()) {
            Tracker tracker = application.getLanalytics().getDefaultTracker();
            tracker.send(event, UserManager.getToken());
        } else {
            if (Config.DEBUG) {
                Log.i(TAG, "Couldn't track event " + event.verb + ". No user login found or tracking is disabled for this build.");
            }
        }
    }

    private static boolean isTrackingEnabled() {
        return BuildConfig.X_TYPE == BuildType.RELEASE
                && (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP);
    }

    public static Lanalytics.Event.Builder newEventBuilder() {
        GlobalApplication application = GlobalApplication.getInstance();
        Lanalytics.Event.Builder builder = new Lanalytics.Event.Builder(application);

        if (UserManager.isLoggedIn()) {
            UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
            builder.setUser(userStorage.getUser().id);
        }

        builder.putContext(CONTEXT_CLIENT_ID, application.getClientId());

        return builder;
    }

    private static String formatTime(int millis) {
        return String.valueOf(millis / 1000.);
    }

    public static String getContextDataJson() {
        GlobalApplication application = GlobalApplication.getInstance();

        Map<String, String> contextData = application.getLanalytics().getDefaultContextData();
        contextData.put(CONTEXT_CLIENT_ID, application.getClientId());

        Gson gson = new Gson();
        return gson.toJson(contextData);
    }

}
