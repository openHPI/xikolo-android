package de.xikolo.config;

import com.google.android.gms.cast.CastMediaControlIntent;

import de.xikolo.BuildConfig;
import de.xikolo.App;
import de.xikolo.R;

public class Config {

    public static final boolean DEBUG = BuildConfig.X_TYPE == BuildType.DEBUG;

    public static final String FONT_DIR = "fonts/";
    public static final String FONT_XIKOLO = "xikolo.ttf";
    public static final String FONT_MATERIAL = "materialdesign.ttf";

    public static final String HOST;
    public static final String HOST_URL;
    public static final String API_URL;

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
        HOST_URL = "https://" + HOST + "/";
        API_URL = HOST_URL + "api/v2/";
    }

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_VALUE_JSON = "application/json";
    public static final String HEADER_ACCEPT_VALUE_JSON_API = "application/vnd.api+json";

    public static final String HEADER_AUTH = "Authorization";
    public static final String HEADER_AUTH_VALUE_PREFIX = "Token token=";
    public static final String HEADER_AUTH_VALUE_PREFIX_JSON_API = "Legacy-Token token=";

    public static final String HEADER_USER_PLATFORM = "X-User-Platform";
    public static final String HEADER_USER_PLATFORM_VALUE = "Android";

    public static final String LANALYTICS_CONTEXT_COOKIE = "lanalytics-context";
    public static final String LANALYTICS_PATH = "tracking-events/";

    public static final String NEWS = "news/";
    public static final String LOGIN = "login/";
    public static final String ACCOUNT = "account/";
    public static final String NEW = "new/";
    public static final String RESET = "reset/" + NEW;
    public static final String COURSES = "courses/";
    public static final String DISCUSSIONS = "pinboard/";
    public static final String ANNOUNCEMENTS = "announcements/";
    public static final String COLLAB_SPACE = "learning_rooms/";
    public static final String ITEMS = "items/";
    public static final String RECAP = "learn?course_id=";

    public static final String WEBSOCKET_URL = "wss://" + HOST + "/ws";

    public static boolean PRESENTER_LIFECYCLE_LOGGING = false;
    public static boolean WEBVIEW_LOGGING = false;
    public static boolean JOB_HELPER_LOGGING = false;

}
