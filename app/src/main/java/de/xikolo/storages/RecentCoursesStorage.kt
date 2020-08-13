package de.xikolo.storages

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.xikolo.storages.base.BaseStorage
import de.xikolo.utils.ShortcutUtil.MAX_SHORTCUTS

typealias RecentCourse = Pair<String, String>

class RecentCoursesStorage : BaseStorage(PREF_RECENT_COURSES, Context.MODE_PRIVATE) {

    companion object {
        private const val PREF_RECENT_COURSES = "preference_recent_courses"
        fun addCourseToRecentCourses(
            courseId: String,
            title: String,
            courses: LinkedHashSet<RecentCourse>
        ): LinkedHashSet<RecentCourse> {
            return courses
                // filter duplicates
                .filter { it.first != courseId }
                .toMutableSet()
                // add visited course
                .apply { this.add(Pair(courseId, title)) }
                // take last MAX_SHORTCUT courses
                .reversed()
                .take(MAX_SHORTCUTS)
                .reversed()
                .toMutableSet() as LinkedHashSet<RecentCourse>
        }
    }

    val recentCourses: LinkedHashSet<RecentCourse>
        get() {
            val json = getString(PREF_RECENT_COURSES)
            val type = object : TypeToken<LinkedHashSet<RecentCourse>>() {}.type
            if (json == null || type == null) {
                return LinkedHashSet()
            }
            return Gson().fromJson(json, type)
        }

    fun addCourse(courseId: String, title: String) {
        var courses: LinkedHashSet<RecentCourse> = recentCourses
        courses = addCourseToRecentCourses(courseId, title, courses)
        putString(PREF_RECENT_COURSES, Gson().toJson(courses))
    }
}
