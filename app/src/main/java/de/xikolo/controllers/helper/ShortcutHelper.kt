package de.xikolo.controllers.helper

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.storages.RecentCoursesStorage

const val MAX_SHORTCUTS = 4

class ShortcutHelper {

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun addCourse(context: Context, courseId: String, title: String) {

        val recentCoursesStorage = RecentCoursesStorage()
        recentCoursesStorage.addCourse(courseId, title)

        updateShortcuts(context)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun updateShortcuts(context: Context) {
        val shortcutManager =
            getSystemService(context, ShortcutManager::class.java)
        val recentCourses = RecentCoursesStorage().recentCourses
        val intentList = mutableListOf<ShortcutInfo>()

        for (course in recentCourses) {
            val courseId = course.first
            val title = course.second

            val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(context)
            intent.action = Intent.ACTION_VIEW

            val shortcut = ShortcutInfo.Builder(context, courseId)
                .setShortLabel(title)
                .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_course))
                .setIntent(intent)
                .build()

            intentList.add(shortcut)
        }
        shortcutManager?.dynamicShortcuts = intentList
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun configureShortcuts(context: Context) {
        val shortcutManager =
            ContextCompat.getSystemService(context, ShortcutManager::class.java)
        if (shortcutManager?.dynamicShortcuts?.isEmpty()!!) {
            ShortcutHelper().updateShortcuts(context)
        }
    }
}
