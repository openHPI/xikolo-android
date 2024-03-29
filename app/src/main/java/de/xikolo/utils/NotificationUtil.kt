package de.xikolo.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.controllers.downloads.DownloadsActivity
import de.xikolo.receivers.NotificationDeletedReceiver
import de.xikolo.storages.NotificationStorage

class NotificationUtil(base: Context) : ContextWrapper(base) {

    companion object {
        const val DOWNLOADS_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".downloads"
        const val DOWNLOAD_RUNNING_NOTIFICATION_ID = 1000
        const val DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID = 1001
        const val DOWNLOAD_COMPLETED_NOTIFICATION_GROUP = "download_completed"

        const val NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE = "key_notification_deleted_title"
        const val NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL = "key_notification_deleted_all"

        fun deleteDownloadNotificationsFromIntent(intent: Intent) {
            val notificationStorage = NotificationStorage()

            val title = intent.getStringExtra(NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE)
            if (title != null) {
                notificationStorage.deleteDownloadNotification(title)
            } else if (intent.getStringExtra(NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL) != null) {
                notificationStorage.delete()
            }
        }
    }

    private var notificationManager: NotificationManager? = null

    private fun getManager(): NotificationManager? {
        if (notificationManager == null) {
            synchronized(NotificationUtil::class.java) {
                if (notificationManager == null) {
                    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                }
            }
        }
        return notificationManager
    }

    init {
        createChannels()
    }

    fun notify(id: Int, notification: Notification?) {
        getManager()?.notify(id, notification)
    }

    fun cancel(id: Int) {
        getManager()?.cancel(id)
    }

    fun showDownloadCompletedNotification(title: String) {
        val notificationStorage = NotificationStorage()

        notificationStorage.addDownloadNotification(title)
        notify(title.hashCode(), getDownloadCompletedNotification(title).build())

        val downloadList = notificationStorage.downloadNotifications
        if (downloadList!!.size > 1) {
            showDownloadSummaryNotification(downloadList)
        }
    }

    private fun getDownloadCompletedNotification(title: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_download_completed))
            .setContentText(title)
            .setColor(ContextCompat.getColor(this, R.color.apptheme_primary))
            .setSmallIcon(R.drawable.ic_download)
            .setAutoCancel(true)
            .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
            .setContentIntent(
                createDownloadCompletedContentIntent(
                    DownloadsActivity::class.java,
                    NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE,
                    title
                )
            )
            .setDeleteIntent(
                createDownloadCompletedDeleteIntent(
                    NOTIFICATION_DELETED_KEY_DOWNLOAD_TITLE,
                    title
                )
            )
    }

    private fun showDownloadSummaryNotification(notifications: List<String>) {
        notify(DOWNLOAD_COMPLETED_SUMMARY_NOTIFICATION_ID, getDownloadSummaryNotification(notifications).build())
    }

    private fun getDownloadSummaryNotification(notifications: List<String>): NotificationCompat.Builder {
        val title: String
        if (notifications.size > 1) {
            title = String.format(getString(R.string.notification_multiple_downloads_completed), notifications.size)
        } else {
            title = getString(R.string.notification_download_completed)
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        for (notification in notifications) {
            inboxStyle.addLine(notification)
        }

        return NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(getString(R.string.app_name))
            .setColor(ContextCompat.getColor(this, R.color.apptheme_primary))
            .setSmallIcon(R.drawable.ic_download)
            .setAutoCancel(true)
            .setGroup(DOWNLOAD_COMPLETED_NOTIFICATION_GROUP)
            .setGroupSummary(true)
            .setStyle(inboxStyle)
            .setContentIntent(createDownloadCompletedContentIntent(DownloadsActivity::class.java, NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL, "true"))
            .setDeleteIntent(createDownloadCompletedDeleteIntent(NOTIFICATION_DELETED_KEY_DOWNLOAD_ALL, "true"))
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadsChannel = NotificationChannel(
                DOWNLOADS_CHANNEL_ID,
                getString(R.string.notification_channel_downloads),
                NotificationManager.IMPORTANCE_LOW
            )
            downloadsChannel.setShowBadge(false)

            getManager()?.createNotificationChannel(downloadsChannel)
        }
    }

    private fun createDownloadCompletedContentIntent(parentActivityClass: Class<*>, extraKey: String, extraValue: String): PendingIntent? {
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(this, parentActivityClass)

        resultIntent.putExtra(extraKey, extraValue)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(this)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(parentActivityClass)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return stackBuilder.getPendingIntent(
            0,
            flags
        )
    }

    private fun createDownloadCompletedDeleteIntent(extraKey: String, extraValue: String): PendingIntent {
        val deleteIntent = Intent(this, NotificationDeletedReceiver::class.java)
        deleteIntent.action = NotificationDeletedReceiver.INTENT_ACTION_NOTIFICATION_DELETED
        deleteIntent.putExtra(extraKey, extraValue)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(this, 0, deleteIntent, flags)
    }

}
