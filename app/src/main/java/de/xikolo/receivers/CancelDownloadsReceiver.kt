package de.xikolo.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import de.xikolo.events.AllDownloadsCancelledEvent
import de.xikolo.services.DownloadService
import org.greenrobot.eventbus.EventBus

class CancelDownloadsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent()
        service.component = ComponentName(context, DownloadService::class.java)
        context.stopService(service)
        EventBus.getDefault().post(AllDownloadsCancelledEvent())
    }

}
