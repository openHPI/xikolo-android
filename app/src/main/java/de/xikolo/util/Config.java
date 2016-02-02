package de.xikolo.util;

import com.google.android.gms.cast.CastMediaControlIntent;

import de.xikolo.BuildConfig;
import de.xikolo.data.preferences.UserPreferences;

public class Config {

    public static final boolean DEBUG = BuildConfig.buildType == BuildType.DEBUG;

    public static final String FONT = "fonts/";

    public static final String PREF_USER = UserPreferences.class.getName();

    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static final String HOST;
    public static final String URI;
    public static final String API;

    public static final String CAST_MEDIA_RECEIVER_APPLICATION_ID;

    static {
        switch (BuildConfig.buildFlavor) {
            case OPEN_HPI:
                HOST = "open.hpi.de";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            case OPEN_SAP:
                HOST = "open.sap.com";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            case OPEN_UNE:
                HOST = "openune.cn";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            default: // MOOC_HOUSE
                HOST = "mooc.house";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
        }
        URI = HTTPS + "://" + HOST + "/";
        API = URI + "api/";
    }

    public static final String HEADER_ACCEPT = "ACCEPT";
    public static final String HEADER_AUTHORIZATION = "AUTHORIZATION";
    public static final String HEADER_USER_PLATFORM = "User-Platform";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_ACCEPT_VALUE = "application/vnd.xikolo.v1, application/json";
    public static final String HEADER_AUTHORIZATION_VALUE_SCHEMA = "Token token=";
    public static final String HEADER_USER_PLATFORM_VALUE = "Android";
    public static final String HEADER_CACHE_CONTROL_VALUE = "no-cache";

    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_PUT = "PUT";

    public static final String NEWS = "news/";
    public static final String LOGIN = "login/";
    public static final String ACCOUNT = "account/";
    public static final String NEW = "new/";
    public static final String RESET = "reset/" + NEW;

    public static final String AUTHENTICATE = "authenticate/";

    public static final String COURSES = "courses/";
    public static final String DISCUSSIONS = "pinboard/";
    public static final String ANNOUNCEMENTS = "announcements/";
    public static final String ROOMS = "learning_rooms/";
    public static final String MODULES = "modules/";
    public static final String ITEMS = "items/";

    public static final String USER = "users/me/";
    public static final String ENROLLMENTS = "enrollments/";
    public static final String PROGRESSIONS = "progressions/";

}
