package de.xikolo.managers

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentActivity
import android.util.Log
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.events.DownloadDeletedEvent
import de.xikolo.events.DownloadStartedEvent
import de.xikolo.events.PermissionDeniedEvent
import de.xikolo.events.PermissionGrantedEvent
import de.xikolo.models.AssetDownload
import de.xikolo.models.Download
import de.xikolo.services.DownloadService
import de.xikolo.utils.ExternalStorageUtil
import de.xikolo.utils.FileUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

class DownloadManager(activity: FragmentActivity) {

    companion object {
        val TAG: String = DownloadManager::class.java.simpleName
    }

    private val permissionManager: PermissionManager = PermissionManager(activity)

    private var pendingAction: PendingAction? = null

    init {
        EventBus.getDefault().register(this)
    }

    data class PendingAction(val type: ActionType, val download: AssetDownload)

    enum class ActionType {
        START, DELETE, CANCEL
    }

    val foldersWithDownloads: List<String>
        get() {
            val folders = ArrayList<String>()

            val publicAppFolder = File(
                Environment.getExternalStorageDirectory().absolutePath
                        + File.separator
                        + App.getInstance().getString(R.string.app_name)
            )

            if (publicAppFolder.isDirectory) {
                val files = publicAppFolder.listFiles()
                for (file in files) {
                    if (file.isDirectory) {
                        folders.add(file.absolutePath)
                    }
                }
            }

            return folders
        }

    fun startAssetDownload(download: AssetDownload): Boolean {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                val context = App.getInstance()
                val intent = Intent(context, DownloadService::class.java)

                when {
                    downloadExists(download) -> {
                        ToastUtil.show(R.string.toast_file_already_downloaded)
                        return false
                    }
                    download.url != null -> {
                        FileUtil.createFolderIfNotExists(
                            File(download.filePath.substring(0, download.filePath.lastIndexOf(File.separator)))
                        )

                        val bundle = Bundle()
                        bundle.putString(DownloadService.ARG_TITLE, download.title)
                        bundle.putString(DownloadService.ARG_URL, download.url)
                        bundle.putString(DownloadService.ARG_FILE_PATH, download.filePath)

                        intent.putExtras(bundle)
                        context.startService(intent)

                        if (download is AssetDownload.Course.Item) {
                            LanalyticsUtil.trackDownloadedFile(download)
                        }

                        EventBus.getDefault().post(DownloadStartedEvent(download))

                        return true
                    }
                    else -> {
                        Log.i(TAG, "URL is null, nothing to download")
                        return false
                    }
                }
            } else {
                pendingAction = PendingAction(ActionType.START, download)
                return false
            }
        } else {
            Log.w(TAG, "No write access for external storage")
            ToastUtil.show(R.string.toast_no_external_write_access)
            return false
        }
    }

    fun deleteAssetDownload(download: AssetDownload): Boolean {
        return if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Delete download " + download.filePath)

                if (!downloadExists(download)) {
                    false
                } else {
                    EventBus.getDefault().post(DownloadDeletedEvent(download))
                    val dlFile = File(download.filePath)
                    dlFile.delete()
                }
            } else {
                pendingAction = PendingAction(ActionType.DELETE, download)
                false
            }
        } else {
            Log.w(TAG, "No write access for external storage")
            ToastUtil.show(R.string.toast_no_external_write_access)
            false
        }
    }

    fun cancelAssetDownload(download: AssetDownload) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Cancel download " + download.url!!)

                val downloadService = DownloadService.getInstance()
                downloadService?.cancelDownload(download.url)

                deleteAssetDownload(download)
            } else {
                pendingAction = PendingAction(ActionType.CANCEL, download)
            }
        } else {
            Log.w(TAG, "No write access for external storage")
            ToastUtil.show(R.string.toast_no_external_write_access)
        }
    }

    @Subscribe
    fun onPermissionGrantedEvent(permissionGrantedEvent: PermissionGrantedEvent) {
        if (permissionGrantedEvent.requestCode == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            pendingAction?.let {
                when (it.type) {
                    ActionType.START -> startAssetDownload(it.download)
                    ActionType.DELETE -> deleteAssetDownload(it.download)
                    ActionType.CANCEL -> cancelAssetDownload(it.download)
                }
            }
            pendingAction = null
        }
    }

    @Subscribe
    fun onPermissionDeniedEvent(permissionDeniedEvent: PermissionDeniedEvent) {
        if (permissionDeniedEvent.requestCode == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            pendingAction = null
        }
    }

    fun getDownload(download: AssetDownload): Download? =
        DownloadService.getInstance()?.getDownload(download.url)

    fun downloadRunning(download: AssetDownload): Boolean =
        downloadRunning(download.url)

    fun downloadRunning(url: String?): Boolean =
        DownloadService.getInstance()?.isDownloading(url) == true

    fun downloadExists(download: AssetDownload): Boolean {
        val file = File(download.filePath)
        return file.isFile && file.exists()
    }

    fun getDownloadFile(download: AssetDownload): File? {
        val file = File(download.filePath)

        return if (file.isFile && file.exists()) {
            file
        } else null
    }

}
