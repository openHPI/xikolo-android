@file:JvmName("DisplayUtil")

package de.xikolo.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics

val <T : Context> T.is7inchTablet: Boolean
    get() {
        val displayMetrics = resources.displayMetrics

        val dpHeight = displayMetrics.heightPixels / displayMetrics.density
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density

        return dpHeight >= 600 || dpWidth >= 600
    }

val <T : Activity?> T.displaySize: Point
    get() {
        val size = Point(0, 0)
        this?.windowManager?.defaultDisplay?.getSize(size)
        return size
    }

val <T : Activity> T.videoThumbnailSize: Point
    get() {
        var size = displaySize
        size = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Point(size.x, size.x / 16 * 9)
        } else {
            Point((size.x * 0.6).toInt(), (size.x * 0.6 / 16 * 9).toInt())
        }
        return size
    }

val <T : Activity> T.aspectRatio: Float
    get() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return if (resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            displayMetrics.heightPixels.toFloat() / displayMetrics.widthPixels
        } else {
            displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels
        }
    }

val <T : Activity> T.hasDisplayCutouts: Boolean
    get() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && window.decorView.rootWindowInsets.displayCutout != null
    }
