package de.xikolo.util;

import android.net.Uri;

public class DeepLinkingUtil {

    private static final String ROUTE_COURSE = "/" + Config.COURSES;

    public static String getCourseIdentifierFromUri(Uri uri) {
        return uri.getPath().replace(ROUTE_COURSE, "");
    }

}
