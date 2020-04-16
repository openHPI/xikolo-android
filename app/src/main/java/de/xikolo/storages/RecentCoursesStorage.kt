package de.xikolo.storages

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.xikolo.App
import de.xikolo.storages.base.BaseStorage
import de.xikolo.storages.base.SharedPreferenceLiveData

typealias RecentCourse = Pair<String, String>

const val MAX_SHORTCUTS = 3

class RecentCoursesStorage : BaseStorage(PREF_RECENT_COURSES, Context.MODE_PRIVATE) {

    val recentCourses: LinkedHashSet<RecentCourse>
        get() {
            val json = getString(PREF_RECENT_COURSES)
            val type = object : TypeToken<LinkedHashSet<RecentCourse>>() {}.type
            if (json == null || type == null) {
                return LinkedHashSet<RecentCourse>()
            }
            println(json)
            return Gson().fromJson(json, type)
        }

    val coursesLive: SharedPreferenceLiveData<String> by lazy {
        // what name does the preference have? What is the sharedPref Parameter supposed to do?
        SharedPreferenceLiveData.SharedPreferenceStringLiveData(PreferenceManager.getDefaultSharedPreferences(App.instance), PREF_RECENT_COURSES, "")
    }

    fun addCourse(courseId: String, title: String) {
        val courses: LinkedHashSet<RecentCourse> = recentCourses

        when {
            courses.contains(Pair(courseId, title)) -> {
                courses.remove(Pair(courseId, title))
                courses.add(Pair(courseId, title))
            }
            courses.size < MAX_SHORTCUTS -> {
                courses.add(Pair(courseId, title))
            }
            courses.size == MAX_SHORTCUTS -> {
                courses.remove(courses.last())
                courses.add(Pair(courseId, title))
            }
        }

        putString(PREF_RECENT_COURSES, Gson().toJson(courses))
    }

    companion object {
        private const val PREF_RECENT_COURSES = "preference_recent_courses"
    }
}
