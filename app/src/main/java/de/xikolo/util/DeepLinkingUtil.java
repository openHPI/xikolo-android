package de.xikolo.util;

import android.net.Uri;

public class DeepLinkingUtil {

    public static final String ROUTE_COURSES = "/" + "courses";
    public static final String ROUTE_RESUME = "/" + "resume";
    public static final String ROUTE_NEWS = "/" + "news";
    public static final String SLASH = "/";

    private static String path;

    public enum Type {
        ALL_COURSES, NEWS
    }

    public static String getCourseIdentifierFromResumeUri(Uri uri) {

        path = uri.getPath();

        if (path.matches(ROUTE_COURSES + SLASH + ".*" + ROUTE_RESUME)) {

            path = path.replace(ROUTE_COURSES, "").replace(ROUTE_RESUME, "").replace(SLASH, "");

            return path;
        }

        return path;
    }

    public static Type getType(Uri uri) {

        path = uri.getPath();

        if(path.matches(ROUTE_NEWS)) {
            return Type.NEWS;
        } else if(path.matches(ROUTE_COURSES)) {
            return Type.ALL_COURSES;
        }

        return null;
    }

}
