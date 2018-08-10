package de.xikolo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.events.NetworkStateEvent;
import de.xikolo.utils.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isOnline = NetworkUtil.isOnline();
            EventBus.getDefault().postSticky(new NetworkStateEvent(isOnline));
        }
    }

}
