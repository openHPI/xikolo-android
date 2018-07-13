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
import de.xikolo.models.Download
import de.xikolo.models.DownloadAsset
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

    data class PendingAction(val type: ActionType, val downloadAsset: DownloadAsset)

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

    fun startAssetDownload(downloadAsset: DownloadAsset): Boolean {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                val context = App.getInstance()
                val intent = Intent(context, DownloadService::class.java)

                when {
                    downloadExists(downloadAsset) -> {
                        ToastUtil.show(R.string.toast_file_already_downloaded)
                        return false
                    }
                    downloadAsset.url != null -> {
                        FileUtil.createFolderIfNotExists(
                            File(downloadAsset.filePath.substring(0, downloadAsset.filePath.lastIndexOf(File.separator)))
                        )

                        val bundle = Bundle()
                        bundle.putString(DownloadService.ARG_TITLE, downloadAsset.title)
                        bundle.putString(DownloadService.ARG_URL, downloadAsset.url)
                        bundle.putString(DownloadService.ARG_FILE_PATH, downloadAsset.filePath)

                        intent.putExtras(bundle)
                        context.startService(intent)

                        if (downloadAsset is DownloadAsset.Course.Item) {
                            LanalyticsUtil.trackDownloadedFile(downloadAsset)
                        }

                        EventBus.getDefault().post(DownloadStartedEvent(downloadAsset))

                        return true
                    }
                    else -> {
                        Log.i(TAG, "URL is null, nothing to download")
                        return false
                    }
                }
            } else {
                pendingAction = PendingAction(ActionType.START, downloadAsset)
                return false
            }
        } else {
            Log.w(TAG, "No write access for external storage")
            ToastUtil.show(R.string.toast_no_external_write_access)
            return false
        }
    }

    fun deleteAssetDownload(downloadAsset: DownloadAsset): Boolean {
        return if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Delete download " + downloadAsset.filePath)

                if (!downloadExists(downloadAsset)) {
                    false
                } else {
                    EventBus.getDefault().post(DownloadDeletedEvent(downloadAsset))
                    val dlFile = File(downloadAsset.filePath)
                    dlFile.delete()
                }
            } else {
                pendingAction = PendingAction(ActionType.DELETE, downloadAsset)
                false
            }
        } else {
            Log.w(TAG, "No write access for external storage")
            ToastUtil.show(R.string.toast_no_external_write_access)
            false
        }
    }

    fun cancelAssetDownload(downloadAsset: DownloadAsset) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Cancel download " + downloadAsset.url!!)

                val downloadService = DownloadService.getInstance()
                downloadService?.cancelDownload(downloadAsset.url)

                deleteAssetDownload(downloadAsset)
            } else {
                pendingAction = PendingAction(ActionType.CANCEL, downloadAsset)
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
                    ActionType.START    -> startAssetDownload(it.downloadAsset)
                    ActionType.DELETE   -> deleteAssetDownload(it.downloadAsset)
                    ActionType.CANCEL   -> cancelAssetDownload(it.downloadAsset)
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

    fun getDownload(downloadAsset: DownloadAsset): Download? =
        DownloadService.getInstance()?.getDownload(downloadAsset.url)

    fun downloadRunning(downloadAsset: DownloadAsset): Boolean =
        downloadRunning(downloadAsset.url)

    fun downloadRunning(url: String?): Boolean =
        DownloadService.getInstance()?.isDownloading(url) == true

    fun downloadExists(downloadAsset: DownloadAsset): Boolean {
        val file = File(downloadAsset.filePath)
        return file.isFile && file.exists()
    }

    fun getDownloadFile(downloadAsset: DownloadAsset): File? {
        val file = File(downloadAsset.filePath)

        return if (file.isFile && file.exists()) {
            file
        } else null
    }

}
