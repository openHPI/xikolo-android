package de.xikolo.managers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.events.DownloadDeletedEvent
import de.xikolo.events.DownloadStartedEvent
import de.xikolo.events.PermissionDeniedEvent
import de.xikolo.events.PermissionGrantedEvent
import de.xikolo.models.DownloadAsset
import de.xikolo.services.DownloadService
import de.xikolo.utils.FileUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.StorageUtil
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

    fun startAssetDownload(downloadAsset: DownloadAsset): Boolean {
        if (StorageUtil.isStorageWritable(downloadAsset.storage)) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                val context = App.instance
                val intent = Intent(context, DownloadService::class.java)

                when {
                    downloadRunning(downloadAsset) || downloadExists(downloadAsset) -> return false
                    downloadAsset.url != null                                       -> {
                        FileUtil.createFolderIfNotExists(
                            File(downloadAsset.filePath.substring(0, downloadAsset.filePath.lastIndexOf(File.separator)))
                        )

                        val bundle = Bundle()
                        bundle.putString(DownloadService.ARG_TITLE, downloadAsset.title)
                        bundle.putString(DownloadService.ARG_URL, downloadAsset.url)
                        bundle.putString(DownloadService.ARG_FILE_PATH, downloadAsset.filePath)
                        bundle.putBoolean(DownloadService.ARG_SHOW_NOTIFICATION, downloadAsset.showNotification)

                        intent.putExtras(bundle)
                        context.startService(intent)

                        if (downloadAsset is DownloadAsset.Course.Item) {
                            LanalyticsUtil.trackDownloadedFile(downloadAsset)
                        }

                        EventBus.getDefault().post(DownloadStartedEvent(downloadAsset))

                        downloadAsset.secondaryAssets.forEach {
                            startAssetDownload(it)
                        }

                        return true
                    }
                    else                                                            -> {
                        Log.i(TAG, "URL is null, nothing to download")
                        return false
                    }
                }
            } else {
                pendingAction = PendingAction(ActionType.START, downloadAsset)
                return false
            }
        } else {
            val msg = StorageUtil.buildWriteErrorMessage(App.instance)
            Log.w(TAG, msg)
            ToastUtil.show(msg)
            return false
        }
    }

    fun deleteAssetDownload(downloadAsset: DownloadAsset, deleteSecondaryDownloads: Boolean = true): Boolean {
        if (StorageUtil.isStorageWritable(downloadAsset.storage)) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Delete download " + downloadAsset.filePath)

                if (!downloadExists(downloadAsset)) {
                    StorageUtil.cleanStorage(File(downloadAsset.filePath).parentFile)
                    return false
                } else {
                    EventBus.getDefault().post(DownloadDeletedEvent(downloadAsset))

                    if (getDownloadFile(downloadAsset)?.delete() == true) {
                        if (deleteSecondaryDownloads) {
                            downloadAsset.secondaryAssets.forEach {
                                if (downloadAsset.deleteSecondaryAssets(it, this)) {
                                    deleteAssetDownload(it)
                                }
                            }
                        }
                        return true
                    }
                    return false
                }
            } else {
                pendingAction = PendingAction(ActionType.DELETE, downloadAsset)
                return false
            }
        } else {
            val msg = StorageUtil.buildWriteErrorMessage(App.instance)
            Log.w(TAG, msg)
            ToastUtil.show(msg)
            return false
        }
    }

    fun cancelAssetDownload(downloadAsset: DownloadAsset) {
        if (StorageUtil.isStorageWritable(downloadAsset.storage)) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                if (Config.DEBUG) Log.d(TAG, "Cancel download " + downloadAsset.url)
                    if (downloadAsset.url != null) {
                        DownloadService.instance?.cancelDownload(downloadAsset.url)

                        deleteAssetDownload(downloadAsset, false)

                        downloadAsset.secondaryAssets.forEach {
                            cancelAssetDownload(it)
                        }
                    } else {
                        ToastUtil.show(R.string.error_plain)
                    }
            } else {
                pendingAction = PendingAction(ActionType.CANCEL, downloadAsset)
            }
        } else {
            val msg = StorageUtil.buildWriteErrorMessage(App.instance)
            Log.w(TAG, msg)
            ToastUtil.show(msg)
        }
    }

    fun getFoldersWithDownloads(storage: File): List<String> {
        val folders = ArrayList<String>()

        val publicAppFolder = File(storage.absolutePath)

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

    @Subscribe
    fun onPermissionGrantedEvent(permissionGrantedEvent: PermissionGrantedEvent) {
        if (permissionGrantedEvent.requestCode == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            pendingAction?.let {
                when (it.type) {
                    ActionType.START  -> startAssetDownload(it.downloadAsset)
                    ActionType.DELETE -> deleteAssetDownload(it.downloadAsset)
                    ActionType.CANCEL -> cancelAssetDownload(it.downloadAsset)
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

    fun getDownloadTotalBytes(downloadAsset: DownloadAsset): Long {
        var totalBytes = 0L
        val mainDownload = DownloadService.instance?.getDownload(downloadAsset.url)
        totalBytes +=
            if (mainDownload != null && mainDownload.totalBytes > 0L) {
                mainDownload.totalBytes
            } else {
                downloadAsset.size
            }

        downloadAsset.secondaryAssets.forEach {
            val secondaryDownload = DownloadService.instance?.getDownload(it.url)
            totalBytes +=
                if (secondaryDownload != null && secondaryDownload.totalBytes > 0L) {
                    secondaryDownload.totalBytes
                } else {
                    it.size
                }
        }

        return totalBytes
    }

    fun getDownloadWrittenBytes(downloadAsset: DownloadAsset): Long {
        var writtenBytes = 0L
        writtenBytes += DownloadService.instance?.getDownload(downloadAsset.url)?.bytesWritten ?: 0L

        downloadAsset.secondaryAssets.forEach {
            writtenBytes += DownloadService.instance?.getDownload(it.url)?.bytesWritten ?: 0L
        }

        return writtenBytes
    }

    fun downloadRunningWithSecondaryAssets(downloadAsset: DownloadAsset): Boolean {
        var downloading = downloadRunning(downloadAsset)
        downloadAsset.secondaryAssets.forEach {
            downloading = downloading || downloadRunningWithSecondaryAssets(it)
        }
        return downloading
    }

    fun downloadRunning(downloadAsset: DownloadAsset): Boolean {
        return downloadAsset.url != null
                && DownloadService.instance?.isDownloading(downloadAsset.url) == true
    }

    fun downloadExists(downloadAsset: DownloadAsset): Boolean {
        return getDownloadFile(downloadAsset) != null
    }

    fun getDownloadFile(downloadAsset: DownloadAsset): File? {
        val originalStorage: File = downloadAsset.storage

        downloadAsset.storage = StorageUtil.getInternalStorage(App.instance)
        val internalFile = File(downloadAsset.filePath)
        if (internalFile.exists() && internalFile.isFile) {
            downloadAsset.storage = originalStorage
            return internalFile
        }

        val sdcardStorage: File? = StorageUtil.getSdcardStorage(App.instance)
        if (sdcardStorage != null) {
            downloadAsset.storage = sdcardStorage
            val sdcardFile = File(downloadAsset.filePath)
            if (sdcardFile.exists() && sdcardFile.isFile) {
                downloadAsset.storage = originalStorage
                return sdcardFile
            }
        }

        downloadAsset.storage = originalStorage
        return null
    }

}
