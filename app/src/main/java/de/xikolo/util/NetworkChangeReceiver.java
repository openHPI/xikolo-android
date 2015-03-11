package de.xikolo.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import de.xikolo.model.events.NetworkStateEvent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        boolean isOnline = NetworkUtil.isOnline(context);

        EventBus.getDefault().post(new NetworkStateEvent(isOnline));
    }

}
