package de.xikolo.storages

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.xikolo.storages.base.BaseStorage
import java.util.*

class NotificationStorage : BaseStorage(PREF_NOTIFICATIONS, Context.MODE_PRIVATE) {

    val downloadNotifications: MutableList<String>
        get() {
            val json = getString(DOWNLOAD_NOTIFICATIONS)
            val type = object : TypeToken<ArrayList<String>>() {}.type
            return Gson().fromJson(json, type)
        }

    fun addDownloadNotification(notification: String) {
        var notifications: MutableList<String>? = downloadNotifications

        if (notifications == null) {
            notifications = ArrayList()
        }
        notifications.add(notification)

        putString(DOWNLOAD_NOTIFICATIONS, Gson().toJson(notifications))
    }

    fun deleteDownloadNotification(notification: String) {
        val notifications = downloadNotifications
        notifications.remove(notification)
        putString(DOWNLOAD_NOTIFICATIONS, Gson().toJson(notifications))
    }

    companion object {
        private const val PREF_NOTIFICATIONS = "pref_notifications"
        private const val DOWNLOAD_NOTIFICATIONS = "download_notifications"
    }

}
