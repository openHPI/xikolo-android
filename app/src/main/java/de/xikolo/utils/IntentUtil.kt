package de.xikolo.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri


object IntentUtil {

    /**
     * Tries to open a PDF in a dedicated PDF app.
     * Falls back to a simple ACTION_VIEW Intent.
     *
     * @return `true` if an Activity for viewing was started, `false` otherwise.
     */
    @JvmStatic
    fun openDoc(c: Context, url: String): Boolean {
        var intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "application/pdf")

        try {
            c.startActivity(intent)
            return true
        } catch (ignored: ActivityNotFoundException) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                c.startActivity(intent)
                return true
            } catch (ignored: ActivityNotFoundException) {
            }
        }

        return false
    }
}
