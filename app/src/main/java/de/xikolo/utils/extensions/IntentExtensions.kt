package de.xikolo.utils.extensions

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import androidx.fragment.app.FragmentActivity
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.dialogs.UnsupportedIntentDialog
import de.xikolo.controllers.dialogs.UnsupportedIntentDialogAutoBundle
import de.xikolo.utils.FileProviderUtil
import java.io.File

@SuppressLint("QueryPermissionsNeeded") // because we only query own components
fun <T : Intent> T.createChooser(
    context: Context,
    title: String? = null,
    hideSelf: Boolean = false
): Intent? {
    return if (hideSelf) {
        // will include all components from all apps until Q (Android 10)
        // but only own components and selected public ones from R (Android 11) on
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.packageManager.queryIntentActivities(this, PackageManager.MATCH_ALL)
        } else {
            context.packageManager.queryIntentActivities(this, 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // exclude packages explicitly
            val componentsToExclude = resolveInfos
                .filter { it.activityInfo.packageName == context.packageName }
                .map { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }
                .toTypedArray()
            Intent.createChooser(this, title)
                .putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, componentsToExclude)
        } else {
            // include packages explicitly
            val intentsToInclude = resolveInfos
                .filter {
                    it.activityInfo.packageName != context.packageName
                }
                .map {
                    val intent = Intent(this)
                    intent.component =
                        ComponentName(it.activityInfo.packageName, it.activityInfo.name)
                    intent.setPackage(it.activityInfo.packageName)
                    intent
                }
                .toMutableList()

            try {
                Intent.createChooser(intentsToInclude.removeAt(0), title)
                    .putExtra(Intent.EXTRA_INITIAL_INTENTS, intentsToInclude.toTypedArray())
            } catch (e: IndexOutOfBoundsException) {
                null
            }
        }
    } else {
        return Intent.createChooser(this, title)
    }
}

private fun <T : Intent> T.open(
    activity: FragmentActivity,
    forceChooser: Boolean,
    parentFile: File? = null
): Boolean {
    val chooserIntent = createChooser(App.instance)

    fun startActivity(): Boolean {
        return try {
            activity.startActivity(
                if (forceChooser) {
                    chooserIntent
                } else {
                    this
                }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            activity.showToast(R.string.error_plain)
            false
        }
    }

    return if (chooserIntent == null) {
        val dialog = UnsupportedIntentDialogAutoBundle.builder()
            .apply {
                if (parentFile != null) {
                    fileMimeType(type)
                }
            }
            .build()
        dialog.listener = object : UnsupportedIntentDialog.Listener {
            override fun onOpenPathClicked() {
                try {
                    val fileManagerIntent = Intent(ACTION_GET_CONTENT)
                    fileManagerIntent.setDataAndType(
                        FileProviderUtil.getUriForFile(
                            parentFile!!
                        ),
                        "vnd.android.document/directory"
                    )
                    fileManagerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    activity.startActivity(fileManagerIntent)
                } catch (e: Exception) {
                    activity.showToast(R.string.dialog_unsupported_intent_error_no_file_manager)
                }
            }

            override fun onOpenAnywayClicked() {
                startActivity()
            }
        }
        dialog.show(activity.supportFragmentManager, UnsupportedIntentDialog.TAG)
        false
    } else {
        startActivity()
    }
}

fun <T : File> T.open(
    activity: FragmentActivity,
    mimeType: String,
    forceChooser: Boolean
): Boolean {
    val target = Intent(ACTION_VIEW)
    target.setDataAndType(FileProviderUtil.getUriForFile(this), mimeType)
    target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    return target.open(activity, forceChooser, parentFile)
}

fun <T : FragmentActivity> T.openUrl(url: String): Boolean {
    val uri = try {
        Uri.parse(url)
    } catch (e: Exception) {
        return false
    }
    return Intent(ACTION_VIEW, uri).open(this, false)
}

fun <T : Intent> T.includeAuthToken(token: String) {
    val headers = Bundle()
    headers.putString(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + token)
    putExtra(Browser.EXTRA_HEADERS, headers)
}
