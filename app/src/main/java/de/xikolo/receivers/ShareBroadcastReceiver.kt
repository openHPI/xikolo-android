package de.xikolo.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import de.xikolo.utils.LanalyticsUtil

class ShareBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.extras?.let { bundle ->
            val packageName = bundle.getParcelable<ComponentName>(
                "android.intent.extra.CHOSEN_COMPONENT"
            )?.packageName

            val pm = context?.packageManager

            val ai: ApplicationInfo? = try {
                if (packageName != null) {
                    pm?.getApplicationInfo(packageName, 0)
                } else null
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            val applicationName = if (ai != null) {
                pm?.getApplicationLabel(ai).toString()
            } else {
                packageName
            }

            bundle.getString("course_id")?.let { courseId ->
                LanalyticsUtil.trackShareCourseLink(
                    courseId,
                    applicationName
                )
            }
        }
    }
}
