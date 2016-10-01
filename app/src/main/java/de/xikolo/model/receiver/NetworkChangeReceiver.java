package de.xikolo.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        boolean isOnline = NetworkUtil.isOnline(context);

        EventBus.getDefault().postSticky(new NetworkStateEvent(isOnline));
    }

}
