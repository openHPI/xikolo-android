package de.xikolo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import de.xikolo.App
import de.xikolo.utils.NetworkUtil

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val isOnline = NetworkUtil.isOnline()

            if (isOnline) {
                App.instance.state.connectivity.online()
            } else {
                App.instance.state.connectivity.offline()
            }
        }
    }

}
