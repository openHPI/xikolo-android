@file:JvmName("DateExtensions")

package de.xikolo.utils.extensions

import android.content.Context
import android.util.Log
import de.xikolo.R
import de.xikolo.config.Config
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "DateExtensions"

private const val XIKOLO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"

fun <T : Date> T.isBetween(from: Date?, to: Date?): Boolean {
    if (from == null && to == null) {
        return false
    }
    if (from == null) {
        if (this.before(to)) {
            return true
        }
    }
    if (to == null) {
        if (this.after(from)) {
            return true
        }
    }
    return from != null && to != null && this.after(from) && this.before(to)
}

val <T : Date?> T.isPast: Boolean
    get() {
        return this != null && Date().after(this)
    }

val <T : Date?> T.isFuture: Boolean
    get() {
        return this != null && Date().before(this)
    }

val <T : String?> T.asDate: Date?
    get() {
        val dateFm = SimpleDateFormat(XIKOLO_DATE_FORMAT, Locale.getDefault())
        var parsedDate: Date? = null
        try {
            if (this != null) {
                parsedDate = dateFm.parse(this)
            }
        } catch (e: ParseException) {
            if (Config.DEBUG)
                Log.w(TAG, "Failed parsing $this", e)
            parsedDate = null
        }

        return parsedDate
    }

val <T : Date> T.formattedString: String
    get() {
        val dateFm = SimpleDateFormat(XIKOLO_DATE_FORMAT, Locale.getDefault())
        return dateFm.format(this)
    }

val <T : Date> T.localString: String
    get() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault())
        dateFormat.timeZone = Calendar.getInstance().timeZone
        return dateFormat.format(this)!!
    }

val <T : Date> T.utcString: String
    get() {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(this)
    }

fun <T : Date> T.timeLeftUntilString(context: Context): String {
    val millis = this.time - Date().time
    val days = TimeUnit.MILLISECONDS.toDays(millis)
    val hours = TimeUnit.MILLISECONDS.toHours(millis - TimeUnit.DAYS.toMillis(days))
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours))
    return String.format(Locale.getDefault(), context.getString(R.string.time_left_format),
        days,
        hours,
        minutes
    )
}

val <T : Date> T.midnight: Date
    get() {
        val c = Calendar.getInstance()
        c.time = this
        c.set(Calendar.HOUR_OF_DAY, 24)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        return c.time
    }

val <T : Date> T.sevenDaysAhead: Date
    get() {
        val c = Calendar.getInstance()
        c.time = this.midnight
        c.add(Calendar.DAY_OF_YEAR, 7)
        return c.time
    }
