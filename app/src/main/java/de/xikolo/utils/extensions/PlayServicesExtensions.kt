@file:JvmName("PlayServicesUtil")

package de.xikolo.utils.extensions

import android.app.Activity
import android.content.Context

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

fun <T : Activity> T.checkPlayServicesWithDialog(): Boolean {
    val googleAPI = GoogleApiAvailability.getInstance()
    val result = googleAPI.isGooglePlayServicesAvailable(this)

    when (result) {
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, ConnectionResult.SERVICE_DISABLED                                                                -> {
            googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            return false
        }
        ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_MISSING_PERMISSION, ConnectionResult.SERVICE_INVALID, ConnectionResult.SERVICE_UPDATING -> return false
    }

    return true
}

fun <T : Context> T.checkPlayServices(): Boolean {
    val googleAPI = GoogleApiAvailability.getInstance()
    val result = googleAPI.isGooglePlayServicesAvailable(this)
    return result == ConnectionResult.SUCCESS
}
