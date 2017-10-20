package de.xikolo.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import de.xikolo.services.DownloadService;

public class CancelDownloadsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent();
        service.setComponent(new ComponentName(context, DownloadService.class));
        context.stopService(service);
    }

}
