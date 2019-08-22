package de.xikolo.utils

import de.xikolo.controllers.helper.CourseArea

object DeepLinkingUtil {

    private const val ROUTE_COURSES = "/" + "courses"
    private const val ROUTE_NEWS = "/" + "news"
    private const val ROUTE_DASHBOARD = "/" + "dashboard"

    private const val SLASH = "/"

    // Course Routes
    private const val ROUTE_RESUME = "/" + "resume"
    private const val ROUTE_PINBOARD = "/" + "pinboard"
    private const val ROUTE_PROGRESS = "/" + "progress"
    private const val ROUTE_LEARNING_ROOMS = "/" + "learning_rooms"
    private const val ROUTE_ANNOUNCEMENTS = "/" + "announcements"

    enum class AppArea {
        ALL_COURSES, NEWS, MY_COURSES
    }

    fun getCourseIdentifier(path: String?): String? {
        return path?.let { it ->
            if (it.matches("$ROUTE_COURSES$SLASH.*".toRegex())) {
                it
                    .replace(ROUTE_COURSES, "")
                    .replace(ROUTE_RESUME, "")
                    .replace(ROUTE_PINBOARD, "")
                    .replace(ROUTE_PROGRESS, "")
                    .replace(ROUTE_LEARNING_ROOMS, "")
                    .replace(ROUTE_ANNOUNCEMENTS, "")
                    .replace(SLASH, "")
            } else null
        }
    }

    fun getTab(path: String?): CourseArea {
        return path?.let { it ->
            when {
                it.endsWith(ROUTE_RESUME)        -> CourseArea.LEARNINGS
                it.endsWith(ROUTE_PINBOARD)      -> CourseArea.DISCUSSIONS
                it.endsWith(ROUTE_PROGRESS)      -> CourseArea.PROGRESS
                it.endsWith(ROUTE_ANNOUNCEMENTS) -> CourseArea.ANNOUNCEMENTS
                else                             -> CourseArea.COURSE_DETAILS
            }
        } ?: CourseArea.COURSE_DETAILS
    }

    fun getType(path: String?): AppArea? {
        return path?.let { it ->
            when {
                it.matches(ROUTE_NEWS.toRegex())      -> AppArea.NEWS
                it.matches(ROUTE_COURSES.toRegex())   -> AppArea.ALL_COURSES
                it.matches(ROUTE_DASHBOARD.toRegex()) -> AppArea.MY_COURSES
                else                                  -> null
            }
        }
    }

}
