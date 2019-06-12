package de.xikolo.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics

object DisplayUtil {

    @JvmStatic
    fun is7inchTablet(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics

        val dpHeight = displayMetrics.heightPixels / displayMetrics.density
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density

        return dpHeight >= 600 || dpWidth >= 600
    }

    @JvmStatic
    fun is10inchTablet(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics

        val dpHeight = displayMetrics.heightPixels / displayMetrics.density
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density

        return dpHeight >= 720 || dpWidth >= 720
    }

    fun getVideoThumbnailSize(activity: Activity): Point {
        var size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        size = if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Point(size.x, size.x / 16 * 9)
        } else {
            Point((size.x * 0.6).toInt(), (size.x * 0.6 / 16 * 9).toInt())
        }
        return size
    }

    fun getAspectRatio(activity: Activity): Float {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return if(activity.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            displayMetrics.heightPixels.toFloat() / displayMetrics.widthPixels
        } else {
            displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels
        }
    }

    fun hasDisplayCutouts(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && activity.window.decorView.rootWindowInsets.displayCutout != null
    }

}
