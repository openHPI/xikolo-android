package de.xikolo.utils.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import de.xikolo.config.Config


fun <T : Intent> T.createChooser(context: Context, title: String? = null, packagesToHide: Array<String> = emptyArray()): Intent? {
    val fakeIntent = Intent(ACTION_VIEW).apply { data = Uri.parse("http://someurl.com") }
    val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.packageManager.queryIntentActivities(this, PackageManager.MATCH_ALL) +
            context.packageManager.queryIntentActivities(fakeIntent, PackageManager.MATCH_ALL)
    } else {
        context.packageManager.queryIntentActivities(this, 0) +
            context.packageManager.queryIntentActivities(fakeIntent, 0)
    }
    val intents = resolveInfos
        .distinctBy {
            it.activityInfo.packageName
        }
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

    return try {
        val chooserIntent = Intent.createChooser(intents.removeAt(0), title)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        chooserIntent
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}

fun <T : Intent> T.includeAuthToken(token: String) {
    val headers = Bundle()
    headers.putString(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + token)
    putExtra(Browser.EXTRA_HEADERS, headers)
}
