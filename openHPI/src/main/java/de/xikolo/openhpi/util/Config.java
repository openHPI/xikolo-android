package de.xikolo.openhpi.util;

import de.xikolo.openhpi.dataaccess.UserPreferences;

public class Config {

    public static final boolean DEBUG = true;

    public static final String FONT_PATH = "fonts/";
    public static final String FONT_SANS = FONT_PATH + "NeoSansStdRegular.ttf";
    public static final String FONT_SANS_BOLD = FONT_PATH + "NeoSansStdMedium.ttf";

    public static final String PREF_USER = UserPreferences.class.getName();

    public static final String HEADER_ACCEPT = "ACCEPT";
    public static final String HEADER_AUTHORIZATION = "AUTHORIZATION";
    public static final String HEADER_USER_PLATFORM = "User-Platform";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_VALUE_USER_PLATFORM_ANDROID = "Android";
    public static final String HEADER_VALUE_ACCEPT_SAP = "application/vnd.opensap.v1, application/json";
    public static final String HEADER_VALUE_NO_CACHE = "no-cache";

    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";

    public static final String URI_SCHEME_HTTP = "http";
    public static final String URI_SCHEME_HTTPS = "https";

    public static final String URI_HOST_HPI = "openhpi.de";
    public static final String URI_HPI = URI_SCHEME_HTTPS + "://" + URI_HOST_HPI + "/";
    public static final String API_HPI = URI_HPI + "api/";
    public static final String URI_HOST_SAP = "open.sap.com";
    public static final String URI_SAP = URI_SCHEME_HTTPS + "://" + URI_HOST_SAP + "/";
    public static final String API_SAP = URI_SAP + "api/";

    public static final String PATH_NEWS = "news/";

    public static final String PATH_AUTHENTICATE = "authenticate/";

    public static final String PATH_COURSES = "courses/";
    public static final String PATH_ANNOUNCEMENTS = "announcements/";
    public static final String PATH_MODULES = "modules/";
    public static final String PATH_ITEMS = "items/";

    public static final String PATH_USER = "users/me/";
    public static final String PATH_ENROLLMENTS = "enrollments/";
    public static final String PATH_PROGRESSIONS = "progressions/";

}
