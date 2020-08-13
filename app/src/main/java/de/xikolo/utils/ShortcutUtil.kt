package de.xikolo.utils

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
import de.xikolo.controllers.settings.SettingsActivity
import de.xikolo.storages.RecentCoursesStorage

object ShortcutUtil {

    const val MAX_SHORTCUTS = 4

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

        val intentList = recentCourses.map { course ->
            val courseId = course.first
            val title = course.second

            val courseIntent = CourseActivityAutoBundle.builder().courseId(courseId).build(context)
            courseIntent.action = Intent.ACTION_VIEW

            // Current solution has the settings in the backstack
            val courseListIntent = Intent(context, SettingsActivity::class.java)
            courseListIntent.action = Intent.ACTION_VIEW

            /*val fragmentManager = FragmentActivity().supportFragmentManager
        val courseListFragment = CourseListFragmentAutoBundle.builder(CourseListFilter.MY).build()
        val tag = "my_courses"

        val oldFragment = fragmentManager.findFragmentByTag(tag)
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, oldFragment ?: courseListFragment, tag)
        transaction.addToBackStack(tag)
        transaction.commit()
*/
            ShortcutInfo.Builder(context, courseId)
                .setShortLabel(title)
                .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_my_courses))
                .setIntents(arrayOf(courseListIntent, courseIntent))
                .build()
        }

        shortcutManager?.dynamicShortcuts = intentList
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun configureShortcuts(context: Context) {
        val shortcutManager =
            ContextCompat.getSystemService(context, ShortcutManager::class.java)
        if (shortcutManager?.dynamicShortcuts?.isEmpty() == true) {
            updateShortcuts(context)
        }
    }
}
