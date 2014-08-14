package de.xikolo.util;

import de.xikolo.BuildConfig;
import de.xikolo.data.preferences.EnrollmentsPreferences;
import de.xikolo.data.preferences.UserPreferences;

public class Config {

    public static final boolean DEBUG = BuildConfig.buildType == BuildType.DEBUG;

    public static final String FONT = "fonts/";
    public static final String FONT_SANS;
    public static final String FONT_XIKOLO = FONT + "xikolo.ttf";

    public static final String PREF_USER = UserPreferences.class.getName();
    public static final String PREF_ENROLLMENTS = EnrollmentsPreferences.class.getName();

    static {
        if (BuildConfig.buildFlavor == BuildFlavor.OPEN_HPI) {
            FONT_SANS = FONT + "NeoSansStdRegular.ttf";
        } else {
            FONT_SANS = FONT + "BentonSansRegular.ttf";
        }
    }

    public static final String HEADER_ACCEPT = "ACCEPT";
    public static final String HEADER_AUTHORIZATION = "AUTHORIZATION";
    public static final String HEADER_USER_PLATFORM = "User-Platform";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_VALUE_USER_PLATFORM_ANDROID = "Android";
    public static final String HEADER_VALUE_ACCEPT_SAP = "application/vnd.opensap.v1, application/json";
    public static final String HEADER_VALUE_NO_CACHE = "no-cache";
    public static final String HEADER_VALUE_ONLY_CACHE = "only-if-cached";

    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_PUT = "PUT";

    public static final String URI_SCHEME_HTTP = "http";
    public static final String URI_SCHEME_HTTPS = "https";

    public static final String URI_HOST_HPI = "open.hpi.de";
    public static final String URI_HPI = URI_SCHEME_HTTPS + "://" + URI_HOST_HPI + "/";
    public static final String API_HPI = URI_HPI + "api/";
    public static final String URI_HOST_SAP = "open.sap.com";
    public static final String URI_SAP = URI_SCHEME_HTTPS + "://" + URI_HOST_SAP + "/";
    public static final String API_SAP = URI_SAP + "api/";

    public static final String NEWS = "news/";
    public static final String LOGIN = "login/";
    public static final String ACCOUNT = "account/";
    public static final String NEW = "new/";
    public static final String RESET = "reset/" + NEW;

    public static final String AUTHENTICATE = "authenticate/";

    public static final String COURSES = "courses/";
    public static final String DISCUSSIONS = "discussion_topics/";
    public static final String ANNOUNCEMENTS = "announcements/";
    public static final String ROOMS = "groups/";
    public static final String MODULES = "modules/";
    public static final String ITEMS = "items/";

    public static final String USER = "users/me/";
    public static final String ENROLLMENTS = "enrollments/";
    public static final String PROGRESSIONS = "progressions/";

}
