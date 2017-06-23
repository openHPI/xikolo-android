package de.xikolo.config;

import com.google.android.gms.cast.CastMediaControlIntent;

import de.xikolo.BuildConfig;
import de.xikolo.App;
import de.xikolo.R;

public class Config {

    public static final boolean DEBUG = BuildConfig.X_TYPE == BuildType.DEBUG;

    public static final String FONT = "fonts/";
    public static final String FONT_XIKOLO = "xikolo.ttf";
    public static final String FONT_MATERIAL = "materialdesign.ttf";

    public static final String HTTPS = "https";

    public static final String HOST;
    public static final String URI;
    public static final String API;
    public static final String API_V2;

    public static final String COPYRIGHT_URL;
    public static final String IMPRINT_URL;
    public static final String PRIVACY_URL;
    public static final String TERMS_OF_USE_URL;

    public static final String CAST_MEDIA_RECEIVER_APPLICATION_ID;

    static {
        switch (BuildConfig.X_FLAVOR) {
            case OPEN_HPI:
                COPYRIGHT_URL = "https://hpi.de/";
                IMPRINT_URL = "https://open.hpi.de/pages/imprint";
                PRIVACY_URL = "https://open.hpi.de/pages/privacy";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = "EE6FB604";
                break;
            case OPEN_HPI_CN:
                COPYRIGHT_URL = "https://hpi.de/";
                IMPRINT_URL = "https://openhpi.cn/pages/imprint?locale=cn";
                PRIVACY_URL = "https://openhpi.cn/pages/privacy?locale=cn";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = "EE6FB604";
                break;
            case OPEN_SAP:
                COPYRIGHT_URL = "http://go.sap.com/corporate/en/legal/copyright.html";
                IMPRINT_URL = "http://go.sap.com/corporate/en/legal/impressum.html";
                PRIVACY_URL = "http://go.sap.com/corporate/en/legal/privacy.html";
                TERMS_OF_USE_URL = "http://go.sap.com/corporate/en/legal/terms-of-use.html";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = "2C63C05D";
                break;
            case OPEN_WHO:
                COPYRIGHT_URL = "http://who.int/";
                IMPRINT_URL = "https://openwho.org/pages/imprint";
                PRIVACY_URL = "https://openwho.org/pages/privacy";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            case OPEN_UNE:
                COPYRIGHT_URL = "http://www.guofudata.com/";
                IMPRINT_URL = "https://openune.cn/pages/imprint";
                PRIVACY_URL = "https://openune.cn/pages/privacy";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            case MOOC_HOUSE_CN:
                COPYRIGHT_URL = "http://www.guofudata.com/";
                IMPRINT_URL = "https://cnmooc.house/pages/imprint?locale=cn";
                PRIVACY_URL = "https://cnmooc.house/pages/privacy?locale=cn";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
            default: // MOOC_HOUSE
                COPYRIGHT_URL = "https://hpi.de/";
                IMPRINT_URL = "https://mooc.house/pages/imprint";
                PRIVACY_URL = "https://mooc.house/pages/privacy";
                TERMS_OF_USE_URL = "";
                CAST_MEDIA_RECEIVER_APPLICATION_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
                break;
        }
        HOST = App.getInstance().getString(R.string.app_host);
        URI = HTTPS + "://" + HOST + "/";
        API = URI + "api/";
        API_V2 = API + "v2/";
    }

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_USER_PLATFORM = "X-User-Platform";

    public static final String COOKIE_LANALYTICS_CONTEXT = "lanalytics-context";

    public static final String HEADER_ACCEPT_VALUE = "application/vnd.xikolo.v1, application/json";
    public static final String HEADER_ACCEPT_VALUE_API_V2 = "application/vnd.api+json";
    public static final String HEADER_AUTHORIZATION_PREFIX = "Token token=";
    public static final String HEADER_AUTHORIZATION_PREFIX_API_V2 = "Legacy-Token token=";
    public static final String HEADER_CACHE_CONTROL_VALUE = "no-cache";
    public static final String HEADER_USER_PLATFORM_VALUE = "Android";

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
    public static final String QUIZ_RECAP = "learn?course_id=";

    public static final String SUBTITLES = "subtitles/";

    public static final String USER = "users/me/";
    public static final String ENROLLMENTS = "enrollments/";
    public static final String PROGRESSIONS = "progressions/";

    public static final String LANALYTICS = "tracking-events/";

    public static final String WEBSOCKET = "wss://" + HOST + "/ws";

}
