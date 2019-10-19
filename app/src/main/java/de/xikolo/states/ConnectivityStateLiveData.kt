package de.xikolo.states

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import de.xikolo.states.base.LiveDataState
import de.xikolo.utils.extensions.isOnline

class ConnectivityStateLiveData(private val context: Context) : LiveDataState<Boolean>(false) {

    private var connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    // callback for API >= 24
    private var networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            online()
        }

        override fun onLost(network: Network?) {
            super.onLost(network)
            offline()
        }
    }

    // callback for API < 24
    private var networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            state(context.isOnline)
        }
    }

    override fun onActive() {
        super.onActive()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    override fun onInactive() {
        super.onInactive()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } else {
            context.unregisterReceiver(networkChangeReceiver)
        }
    }

    fun online() {
        state(true)
    }

    fun offline() {
        state(false)
    }
}
