package de.xikolo.download.filedownload

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import de.xikolo.App
import de.xikolo.download.DownloadHandler
import de.xikolo.download.DownloadStatus
import de.xikolo.download.filedownload.FileDownloadRequest.Companion.REQUEST_EXTRA_SHOW_NOTIFICATION
import de.xikolo.download.filedownload.FileDownloadRequest.Companion.REQUEST_EXTRA_TITLE
import de.xikolo.utils.NotificationUtil

object FileDownloadHandler : DownloadHandler<FileDownloadIdentifier, FileDownloadRequest> {

    private val disabledNotificationsConfiguration =
        FetchConfiguration.Builder(App.instance)
            .setAutoRetryMaxAttempts(1)
            .setDownloadConcurrentLimit(5)
            .enableFileExistChecks(false)
            .setNamespace("XIKOLO_FETCH_NO_NOTIFICATIONS")
            .build()

    private val enabledNotificationsConfiguration =
        FetchConfiguration.Builder(App.instance)
            .setAutoRetryMaxAttempts(1)
            .setDownloadConcurrentLimit(5)
            .enableFileExistChecks(false)
            .setNamespace("XIKOLO_FETCH")
            .setNotificationManager(
                // Use custom patched version instead of default notification manager
                // due to an open issue in the external dependency.
                object : PatchedFetchNotificationManager(App.instance) {
                    override fun getFetchInstanceForNamespace(namespace: String): Fetch {
                        return enabledNotificationsManager
                    }

                    override fun getDownloadNotificationTitle(download: Download): String {
                        return download.request.extras.getString(
                            REQUEST_EXTRA_TITLE,
                            download.request.url
                        )
                    }

                    override fun shouldCancelNotification(
                        downloadNotification: DownloadNotification
                    ): Boolean {
                        return downloadNotification.isCompleted
                    }

                    override fun getChannelId(notificationId: Int, context: Context): String {
                        NotificationUtil(context)
                        return NotificationUtil.DOWNLOADS_CHANNEL_ID
                    }
                }
            )
            .build()

    private val enabledNotificationsManager: Fetch = Fetch.getInstance(
        enabledNotificationsConfiguration
    )

    private val disabledNotificationsManager: Fetch = Fetch.getInstance(
        disabledNotificationsConfiguration
    )

    init {
        val listener = object : FetchListener {
            override fun onAdded(download: Download) {
                notifyStatus(download)
            }

            override fun onCancelled(download: Download) {
                cancel(FileDownloadIdentifier(download.id))
                notifyStatus(download)
            }

            override fun onCompleted(download: Download) {
                if (download.extras.getBoolean(REQUEST_EXTRA_SHOW_NOTIFICATION, true)) {
                    NotificationUtil(App.instance).showDownloadCompletedNotification(
                        download.extras.getString(REQUEST_EXTRA_TITLE, download.file)
                    )
                }
                notifyStatus(download)
            }

            override fun onDeleted(download: Download) {
                notifyStatus(download)
            }

            override fun onDownloadBlockUpdated(
                download: Download,
                downloadBlock: DownloadBlock,
                totalBlocks: Int
            ) {
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                notifyStatus(download)
            }

            override fun onPaused(download: Download) {
                notifyStatus(download)
            }

            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                notifyStatus(download)
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                notifyStatus(download)
            }

            override fun onRemoved(download: Download) {}

            override fun onResumed(download: Download) {
                notifyStatus(download)
            }

            override fun onStarted(
                download: Download,
                downloadBlocks: List<DownloadBlock>,
                totalBlocks: Int
            ) {
                notifyStatus(download)
            }

            override fun onWaitingNetwork(download: Download) {}
        }

        disabledNotificationsManager.addListener(listener)
        enabledNotificationsManager.addListener(listener)
    }

    private val listeners: MutableMap<Int, ((DownloadStatus?) -> Unit)?> = mutableMapOf()

    override fun isDownloadingAnything(callback: (Boolean) -> Unit) {
        disabledNotificationsManager.hasActiveDownloads(true) { a ->
            enabledNotificationsManager.hasActiveDownloads(true) { b ->
                callback(a || b)
            }
        }
    }

    override fun download(
        request: FileDownloadRequest,
        listener: ((DownloadStatus?) -> Unit)?,
        callback: ((FileDownloadIdentifier?) -> Unit)?
    ) {
        val req = request.buildRequest()

        if (req.extras.getBoolean(REQUEST_EXTRA_SHOW_NOTIFICATION, true)) {
            enabledNotificationsManager
        } else {
            disabledNotificationsManager
        }.enqueue(
            req,
            {
                callback?.invoke(FileDownloadIdentifier(it.id))
            },
            {
                callback?.invoke(null)
            }
        )

        listeners[req.id] = listener
    }

    override fun cancel(identifier: FileDownloadIdentifier, callback: ((Boolean) -> Unit)?) {
        enabledNotificationsManager.getDownload(identifier.id) { d1 ->
            if (d1 != null) {
                enabledNotificationsManager.cancel(d1.id)
                enabledNotificationsManager.delete(d1.id)
                callback?.invoke(true)
            } else {
                disabledNotificationsManager.getDownload(identifier.id) { d2 ->
                    if (d2 != null) {
                        disabledNotificationsManager.cancel(d2.id)
                        disabledNotificationsManager.delete(d2.id)
                        callback?.invoke(true)
                    } else {
                        callback?.invoke(false)
                    }
                }
            }
        }
    }

    override fun status(identifier: FileDownloadIdentifier, callback: (DownloadStatus?) -> Unit) {
        disabledNotificationsManager.getDownload(identifier.id) { a ->
            enabledNotificationsManager.getDownload(identifier.id) { b ->
                callback(
                    getDownloadStatus(a ?: b)
                )
            }
        }
    }

    private fun notifyStatus(download: Download) {
        listeners[download.id]?.invoke(
            getDownloadStatus(download)
        )
    }

    private fun getDownloadStatus(download: Download?): DownloadStatus? {
        return try {
            download!!
            DownloadStatus(
                download.total,
                download.downloaded,
                when (download.status) {
                    Status.ADDED, Status.QUEUED -> DownloadStatus.State.PENDING
                    Status.FAILED -> DownloadStatus.State.FAILED
                    Status.COMPLETED -> DownloadStatus.State.SUCCESSFUL
                    Status.CANCELLED -> DownloadStatus.State.CANCELLED
                    Status.DOWNLOADING, Status.PAUSED -> DownloadStatus.State.RUNNING
                    else -> throw Exception()
                }
            )
        } catch (e: Exception) {
            null
        }
    }
}
