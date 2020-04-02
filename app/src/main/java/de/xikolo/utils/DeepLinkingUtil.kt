package de.xikolo.utils

import android.net.Uri
import de.xikolo.config.Feature
import de.xikolo.controllers.helper.CourseArea

object DeepLinkingUtil {

    private const val ROUTE_COURSES = "/courses"
    private const val ROUTE_NEWS = "/news"
    private const val ROUTE_DASHBOARD = "/dashboard"
    private const val ROUTE_RECAP = "/learn"

    private const val SLASH = "/"

    private const val ROUTE_COURSE_RESUME = "/resume"
    private const val ROUTE_COURSE_PINBOARD = "/pinboard"
    private const val ROUTE_COURSE_PROGRESS = "/progress"
    private const val ROUTE_COURSE_ANNOUNCEMENTS = "/announcements"
    private const val ROUTE_COURSE_ITEMS = "/items"

    enum class AppArea {
        ALL_COURSES, NEWS, MY_COURSES
    }

    fun getCourseIdentifier(uri: Uri?): String? {
        return uri?.let {
            it.path?.let { path ->
                if (path.matches("$ROUTE_COURSES$SLASH.*".toRegex())) {
                    path
                        .replace(ROUTE_COURSES, "")
                        .replace(ROUTE_COURSE_RESUME, "")
                        .replace(ROUTE_COURSE_PINBOARD, "")
                        .replace(ROUTE_COURSE_PROGRESS, "")
                        .replace(ROUTE_COURSE_ANNOUNCEMENTS, "")
                        .replaceAfter(ROUTE_COURSE_ITEMS, "")
                        .replace(ROUTE_COURSE_ITEMS, "")
                        .replace(SLASH, "")
                } else if (path.matches("$ROUTE_RECAP.*".toRegex()) && Feature.enabled("recap")) {
                    it.getQueryParameter("course_id")
                } else null
            }
        }
    }

    fun getItemIdentifier(path: String?): String? {
        return path?.let {
            if (it.matches("$ROUTE_COURSES$SLASH..*$ROUTE_COURSE_ITEMS$SLASH..*".toRegex())) {
                it
                    .replaceBefore(ROUTE_COURSE_ITEMS, "")
                    .replace(ROUTE_COURSE_ITEMS, "")
                    .replace(SLASH, "")
            } else null
        }
    }

    fun getTab(path: String?): CourseArea {
        return path?.let { it ->
            when {
                it.endsWith(ROUTE_COURSE_RESUME)                                   -> CourseArea.LEARNINGS
                it.endsWith(ROUTE_COURSE_PINBOARD)                                 -> CourseArea.DISCUSSIONS
                it.endsWith(ROUTE_COURSE_PROGRESS)                                 -> CourseArea.PROGRESS
                it.endsWith(ROUTE_COURSE_ANNOUNCEMENTS)                            -> CourseArea.ANNOUNCEMENTS
                it.matches("$ROUTE_RECAP.*".toRegex()) && Feature.enabled("recap") -> CourseArea.RECAP
                else                                                               -> CourseArea.COURSE_DETAILS
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
