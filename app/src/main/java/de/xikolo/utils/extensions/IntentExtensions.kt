package de.xikolo.utils.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Browser
import de.xikolo.config.Config


fun <T : Intent> T.createChooser(context: Context, title: String? = null, packagesToHide: Array<String> = emptyArray()): Intent {
    val resolveInfos = context.packageManager.queryIntentActivities(this, 0)
    val intents = resolveInfos
        .filter {
            !packagesToHide.contains(it.activityInfo.packageName)
        }
        .map {
            val intent = Intent(this)
            intent.component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
            intent.setPackage(it.activityInfo.packageName)
            intent
        }
        .toMutableList()

    val chooserIntent = Intent.createChooser(intents.removeAt(0), title)
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
    return chooserIntent
}

fun <T : Intent> T.includeAuthToken(token: String) {
    val headers = Bundle()
    headers.putString(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + token)
    putExtra(Browser.EXTRA_HEADERS, headers)
}
