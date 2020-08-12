package de.xikolo.download.filedownload

import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import de.xikolo.App
import de.xikolo.download.DownloadItem
import de.xikolo.download.DownloadStatus
import de.xikolo.extensions.observeOnce
import de.xikolo.managers.PermissionManager
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Storage
import de.xikolo.states.PermissionStateLiveData
import de.xikolo.utils.FileProviderUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.extensions.buildWriteErrorMessage
import de.xikolo.utils.extensions.createIfNotExists
import de.xikolo.utils.extensions.internalStorage
import de.xikolo.utils.extensions.preferredStorage
import de.xikolo.utils.extensions.sdcardStorage
import de.xikolo.utils.extensions.showToast
import java.io.File

open class FileDownloadItem(
    val url: String?,
    open val fileName: String,
    var storage: Storage = App.instance.preferredStorage
) : DownloadItem<File, FileDownloadIdentifier>() {

    companion object {
        val TAG: String? = DownloadAsset::class.simpleName
    }

    protected open fun getFileFolder(): String {
        return storage.file.absolutePath
    }

    val filePath: String
        get() = getFileFolder() + File.separator + fileName

    protected open val size: Long = 0L

    protected open val mimeType = "application/pdf"

    protected open val showNotification = true

    protected open val secondaryDownloadItems = setOf<FileDownloadItem>()

    protected open val deleteSecondaryDownloadItemPredicate: (FileDownloadItem) -> Boolean =
        { _ -> true }

    private var downloader: FileDownloadHandler = FileDownloadHandler

    private val request
        get() = FileDownloadRequest(
            url!!,
            File("$filePath.tmp"),
            title,
            showNotification
        )

    private var downloadIdentifier: FileDownloadIdentifier? = null

    private fun getDownloadIdentifier(): FileDownloadIdentifier {
        return downloadIdentifier ?: FileDownloadIdentifier(request.buildRequest().id)
    }

    override val isDownloadable: Boolean
        get() = url != null

    override val title: String
        get() = fileName

    override val downloadSize: Long
        get() {
            var total = size
            secondaryDownloadItems.forEach { total += it.downloadSize }
            return total
        }

    override val download: File?
        get() {
            val originalStorage: Storage = storage

            storage = App.instance.internalStorage
            val internalFile = File(filePath)
            if (internalFile.exists() && internalFile.isFile) {
                storage = originalStorage
                return internalFile
            }

            val sdcardStorage: Storage? = App.instance.sdcardStorage
            if (sdcardStorage != null) {
                storage = sdcardStorage
                val sdcardFile = File(filePath)
                if (sdcardFile.exists() && sdcardFile.isFile) {
                    storage = originalStorage
                    return sdcardFile
                }
            }

            storage = originalStorage
            return null
        }

    override val openIntent: Intent?
        get() {
            return download?.let {
                val target = Intent(Intent.ACTION_VIEW)
                target.setDataAndType(FileProviderUtil.getUriForFile(it), mimeType)
                target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                Intent.createChooser(target, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

    override var stateListener: StateListener? = null

    override fun start(activity: FragmentActivity, callback: ((FileDownloadIdentifier?) -> Unit)?) {
        performAction(activity) {
            isDownloadRunning { isDownloadRunning ->
                when {
                    isDownloadRunning || downloadExists -> callback?.invoke(null)
                    isDownloadable -> {
                        File(
                            filePath.substring(
                                0,
                                filePath.lastIndexOf(File.separator)
                            )
                        ).createIfNotExists()

                        downloader.download(
                            request,
                            { status ->
                                when (status?.state) {
                                    DownloadStatus.State.CANCELLED ->
                                        cancel(activity)
                                    DownloadStatus.State.SUCCESSFUL -> {
                                        File("$filePath.tmp").renameTo(File(filePath))
                                        stateListener?.onCompleted()
                                    }
                                    DownloadStatus.State.FAILED ->
                                        stateListener?.onCompleted()
                                }
                            },
                            { identifier ->
                                if (identifier != null) {
                                    downloadIdentifier = identifier

                                    if (this is DownloadAsset.Course.Item) {
                                        LanalyticsUtil.trackDownloadedFile(this)
                                    }

                                    secondaryDownloadItems.forEach {
                                        it.start(activity)
                                    }

                                    stateListener?.onStarted()
                                    callback?.invoke(identifier)
                                } else {
                                    callback?.invoke(null)
                                }
                            }
                        )
                    }
                    else -> callback?.invoke(null)
                }
            }
        }
    }

    override fun cancel(activity: FragmentActivity, callback: ((Boolean) -> Unit)?) {
        performAction(activity) {
            downloader.cancel(getDownloadIdentifier()) { success ->
                delete(activity)
                secondaryDownloadItems.forEach {
                    it.cancel(activity)
                }

                callback?.invoke(success)
            }
        }
    }

    override fun delete(activity: FragmentActivity, callback: ((Boolean) -> Unit)?) {
        performAction(activity) {
            downloader.cancel(getDownloadIdentifier()) {
                if (!downloadExists) {
                    File(filePath).parentFile?.let {
                        Storage(it).clean()
                    }

                    stateListener?.onDeleted()
                    callback?.invoke(false)
                } else {
                    if (download?.delete() == true) {
                        secondaryDownloadItems.forEach {
                            if (deleteSecondaryDownloadItemPredicate(it)) {
                                it.delete(activity)
                            }
                        }
                    }

                    stateListener?.onDeleted()
                    callback?.invoke(true)
                }
            }
        }
    }

    override fun getProgress(callback: (Pair<Long?, Long?>) -> Unit) {
        status {
            callback(it?.downloadedBytes to it?.totalBytes)
        }
    }

    override fun isDownloadRunning(callback: (Boolean) -> Unit) {
        status {
            callback(
                if (it != null) {
                    it.state == DownloadStatus.State.RUNNING ||
                        it.state == DownloadStatus.State.PENDING
                } else false
            )
        }
    }

    private fun status(callback: (DownloadStatus?) -> Unit) {
        var totalBytes = 0L
        var writtenBytes = 0L
        var state = DownloadStatus.State.SUCCESSFUL

        downloader.status(getDownloadIdentifier()) { mainDownload ->
            if (mainDownload != null && mainDownload.totalBytes > 0L) {
                totalBytes += mainDownload.totalBytes
                writtenBytes += mainDownload.downloadedBytes
                state = state.and(mainDownload.state)
            } else {
                totalBytes += size
            }

            secondaryDownloadItems.forEach {
                it.status { status ->
                    if (status != null) {
                        totalBytes += status.totalBytes
                        writtenBytes += status.downloadedBytes
                        state = state.and(status.state)

                        callback(
                            DownloadStatus(
                                totalBytes,
                                writtenBytes,
                                state
                            )
                        )
                    }
                }
            }

            callback(
                DownloadStatus(
                    totalBytes,
                    writtenBytes,
                    state
                )
            )
        }
    }

    private fun performAction(activity: FragmentActivity, action: () -> Unit): Boolean {
        return if (storage.isWritable) {
            if (PermissionManager(activity)
                    .requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1
            ) {
                action()
                true
            } else {
                App.instance.state.permission.of(
                    PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                )
                    .observeOnce(activity) { state ->
                        return@observeOnce if (
                            state == PermissionStateLiveData.PermissionStateCode.GRANTED
                        ) {
                            performAction(activity, action)
                            true
                        } else false
                    }
                false
            }
        } else {
            val msg = App.instance.buildWriteErrorMessage()
            Log.w(TAG, msg)
            activity.showToast(msg)
            false
        }
    }
}
