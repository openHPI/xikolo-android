package de.xikolo.utils.extensions

import android.content.Context
import de.xikolo.R
import java.util.*
import java.util.concurrent.TimeUnit

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
