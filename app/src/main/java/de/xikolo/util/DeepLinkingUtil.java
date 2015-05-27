package de.xikolo.util;

import android.net.Uri;

public class DeepLinkingUtil {

    public static final String ROUTE_COURSES = "/" + "courses";
    public static final String ROUTE_NEWS = "/" + "news";

    public static final String SLASH = "/";

    // Course Routes
    public static final String ROUTE_RESUME = "/" + "resume";
    public static final String ROUTE_PINBOARD = "/" + "pinboard";
    public static final String ROUTE_PROGRESS = "/" + "progress";
    public static final String ROUTE_LEARNING_ROOMS = "/" + "learning_rooms";
    public static final String ROUTE_ANNOUNCEMENTS = "/" + "announcements";

    private static String path;

    public enum Type {
        ALL_COURSES, NEWS
    }

    public enum CourseTab {
        RESUME, PINBOARD, PROGRESS, LEARNING_ROOMS, ANNOUNCEMENTS, DETAILS
    }

    public static String getCourseIdentifierFromResumeUri(Uri uri) {

        path = uri.getPath();

        if (path.matches(ROUTE_COURSES + SLASH + ".*")) {

            path = path.replace(ROUTE_COURSES, "").replace(ROUTE_RESUME, "").replace(ROUTE_PINBOARD, "").replace(ROUTE_PROGRESS, "").replace(ROUTE_LEARNING_ROOMS, "").replace(ROUTE_ANNOUNCEMENTS, "").replace(SLASH, "");

            return path;
        }

        return path;
    }

    public static CourseTab getTab(String courseRoute) {

        if (courseRoute.endsWith(ROUTE_RESUME)) {
            return CourseTab.RESUME;
        } else if (courseRoute.endsWith(ROUTE_PINBOARD)) {
            return CourseTab.PINBOARD;
        } else if (courseRoute.endsWith(ROUTE_PROGRESS)) {
            return CourseTab.PROGRESS;
        } else if (courseRoute.endsWith(ROUTE_LEARNING_ROOMS)) {
            return CourseTab.LEARNING_ROOMS;
        } else if (courseRoute.endsWith(ROUTE_ANNOUNCEMENTS)) {
            return CourseTab.ANNOUNCEMENTS;
        }

        return null;
    }

    public static Type getType(Uri uri) {

        path = uri.getPath();

        if (path.matches(ROUTE_NEWS)) {
            return Type.NEWS;
        } else if (path.matches(ROUTE_COURSES)) {
            return Type.ALL_COURSES;
        }

        return null;
    }

}
