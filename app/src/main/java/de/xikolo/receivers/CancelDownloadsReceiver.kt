package de.xikolo.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import de.xikolo.App
import de.xikolo.services.DownloadService

class CancelDownloadsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent()
        service.component = ComponentName(context, DownloadService::class.java)
        context.stopService(service)

        App.instance.state.downloadCancellation.signal()
    }

}
