@file:JvmName("NetworkUtil")

package de.xikolo.utils.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build

enum class ConnectivityType {
    WIFI, CELLULAR, NONE
}

val <T : Context?> T.isOnline: Boolean
    get() {
        val manager = this?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val info = manager.getNetworkCapabilities(manager.activeNetwork)
                ?: return false

            return info.hasCapability(NET_CAPABILITY_INTERNET)
        } else {
            val info = manager.activeNetworkInfo ?: return false
            return info.isConnected
        }
    }

val <T : Context?> T.connectivityType: ConnectivityType
    get() {
        val manager = this?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            ?: return ConnectivityType.NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val info = manager.getNetworkCapabilities(manager.activeNetwork)
                ?: return ConnectivityType.NONE

            if (info.hasTransport(TRANSPORT_WIFI)) {
                return ConnectivityType.WIFI
            }

            if (info.hasTransport(TRANSPORT_CELLULAR)) {
                return ConnectivityType.CELLULAR
            }
        } else {
            val info = manager.activeNetworkInfo ?: return ConnectivityType.NONE

            @Suppress("DEPRECATION")
            if (info.type == ConnectivityManager.TYPE_WIFI) {
                return ConnectivityType.WIFI
            }

            @Suppress("DEPRECATION")
            if (info.type == ConnectivityManager.TYPE_MOBILE) {
                return ConnectivityType.CELLULAR
            }
        }

        return ConnectivityType.NONE
    }
