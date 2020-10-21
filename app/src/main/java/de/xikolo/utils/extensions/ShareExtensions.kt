package de.xikolo.utils.extensions

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.ShareCompat
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.models.dao.CourseDao
import de.xikolo.receivers.ShareBroadcastReceiver
import de.xikolo.utils.LanalyticsUtil

fun <T : Activity> T.shareCourseLink(courseId: String) {
    val intent = ShareCompat.IntentBuilder.from(this)
        .setType("text/plain")
        .setText("${Config.HOST_URL}courses/${CourseDao.Unmanaged.find(courseId)?.slug}")
        .intent

    val chooserIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val receiverIntent = Intent(this, ShareBroadcastReceiver::class.java)
        receiverIntent.putExtra("course_id", courseId)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        Intent.createChooser(
            intent,
            getString(R.string.action_share),
            pendingIntent.intentSender
        )
    } else {
        LanalyticsUtil.trackShareCourseLink(courseId)

        Intent.createChooser(
            intent,
            getString(R.string.action_share)
        )
    }

    startActivity(chooserIntent)
}
