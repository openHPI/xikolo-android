package de.xikolo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import de.xikolo.events.NetworkStateEvent
import de.xikolo.utils.NetworkUtil
import org.greenrobot.eventbus.EventBus

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val isOnline = NetworkUtil.isOnline()
            EventBus.getDefault().postSticky(NetworkStateEvent(isOnline))
        }
    }

}
