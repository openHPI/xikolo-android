package de.xikolo.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import de.xikolo.App
import de.xikolo.config.Config
import de.xikolo.models.Download
import de.xikolo.network.ApiService
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.NotificationUtil
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class DownloadService : Service() {

    companion object {
        val TAG: String = DownloadService::class.java.simpleName

        const val ARG_TITLE = "title"

        const val ARG_URL = "url"

        const val ARG_FILE_PATH = "file_path"

        const val ARG_SHOW_NOTIFICATION = "show_notification"

        var instance: DownloadService? = null
            private set
    }

    private var serviceHandler: ServiceHandler? = null

    private var notificationUtil: NotificationUtil? = null

    private var downloadClient: DownloadManager? = null

    private var downloadMap: ConcurrentHashMap<Int, Download>? = null

    private var jobCounter: AtomicInteger? = null

    val isDownloading: Boolean
        @Synchronized get() = downloadMap?.isNotEmpty() == true

    private val runningDownloadTitles: MutableList<String>
        @Synchronized get() {
            val titles = ArrayList<String>()

            downloadMap?.values?.let { downloads ->
                for (download in downloads) {
                    if (isDownloading(download.url) && download.showNotification) {
                        titles.add(download.title)
                    }
                }
            }

            return titles
        }

    override fun onCreate() {
        instance = this

        if (Config.DEBUG) Log.d(TAG, "DownloadService created")

        jobCounter = AtomicInteger()

        notificationUtil = NotificationUtil(this)

        // Don't use HttpLoggingInterceptor, crashes with OutOfMemoryException!
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiService.authenticationInterceptor)
            .addInterceptor(ApiService.userAgentInterceptor)
            .build()
        downloadClient = DownloadManager.Builder()
            .context(this)
            .downloader(OkHttpDownloader.create(client))
            .build()

        downloadMap = ConcurrentHashMap()

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        // Get the HandlerThread's Looper and use it for our Handler
        val serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Config.DEBUG) Log.d(TAG, "DownloadService start command received")

        intent?.extras?.getString(ARG_TITLE)?.let { title ->
            if (intent.extras?.getBoolean(ARG_SHOW_NOTIFICATION, true) == true) {
                val titles = runningDownloadTitles
                titles.add(title)

                val notification = notificationUtil?.getDownloadRunningNotification(titles)?.build()
                startForeground(NotificationUtil.DOWNLOAD_RUNNING_NOTIFICATION_ID, notification)
            }

            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
            serviceHandler?.obtainMessage()?.let { msg ->
                msg.data = intent.extras
                jobCounter?.incrementAndGet()
                serviceHandler?.sendMessage(msg)
            }
        }

        // If we get killed, after returning from here, restart
        return Service.START_STICKY
    }

    @Synchronized
    private fun stopJob() {
        if (jobCounter?.decrementAndGet() == 0) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        instance = null

        if (Config.DEBUG) Log.d(TAG, "DownloadService destroyed")

        stopForeground(true)
        downloadClient?.cancelAll()
        notificationUtil = null
    }

    @Synchronized
    fun isDownloading(url: String?): Boolean {
        val download = getDownload(url)
        return download?.state == Download.State.PENDING || download?.state == Download.State.RUNNING
    }

    @Synchronized
    fun isDownloadingTempFile(path: File?): Boolean {
        if (path == null) return false

        downloadMap?.values?.let { downloads ->
            for (download in downloads) {
                if (path.absolutePath == download.filePath + ".tmp" && isDownloading(download.url)) {
                    return true
                }
            }
        }

        return false
    }

    @Synchronized
    fun getDownload(url: String?): Download? {
        if (url == null) return null

        downloadMap?.values?.let { downloads ->
            for (download in downloads) {
                if (url == download.url) return download
            }
        }

        return null
    }

    @Synchronized
    fun cancelDownload(url: String) {
        if (Config.DEBUG) Log.i(TAG, "Download cancelled: $url")

        val download = getDownload(url)
        if (download != null && isDownloading(url)) {
            downloadClient?.cancel(download.id)
            downloadMap?.remove(download.id)
            stopJob()
        }
    }

    @Synchronized
    private fun updateDownloadProgress(downloadId: Int, bytesWritten: Long, totalBytes: Long) {
        downloadMap?.get(downloadId)?.also { download ->
            download.state = Download.State.RUNNING
            download.bytesWritten = bytesWritten
            download.totalBytes = totalBytes
        }

        var bytesWrittenOfAll: Long = 0
        var totalBytesOfAll: Long = 0

        downloadMap?.values?.let { downloads ->
            for (download in downloads) {
                bytesWrittenOfAll += download.bytesWritten
                totalBytesOfAll += download.totalBytes
            }
        }

        // Race condition NullPointerException can be thrown
        val notification = notificationUtil
            ?.getDownloadRunningNotification(runningDownloadTitles)
            ?.setProgress(
                100,
                (bytesWrittenOfAll / (totalBytesOfAll / 100.0)).toInt(),
                false
            )
            ?.build()
        notificationUtil?.notify(
            NotificationUtil.DOWNLOAD_RUNNING_NOTIFICATION_ID,
            notification
        )
    }

    @Synchronized
    private fun updateDownloadSuccess(downloadId: Int) {
        downloadMap?.get(downloadId)?.let { download ->
            download.state = Download.State.SUCCESSFUL
            if (download.showNotification) {
                notificationUtil?.showDownloadCompletedNotification(download)
            }

            App.instance.state.download.of(download.url).completed()
        }
    }

    @Synchronized
    private fun updateDownloadFailure(downloadId: Int) {
        downloadMap?.get(downloadId)?.also { download ->
            download.state = Download.State.FAILURE
        }
    }

    // Handler that receives messages from the thread
    private inner class ServiceHandler internal constructor(looper: Looper) : Handler(looper) {

        override fun handleMessage(message: Message) {
            val title = message.data.getString(ARG_TITLE)
            val url = message.data.getString(ARG_URL)
            val filePath = message.data.getString(ARG_FILE_PATH)
            val showNotification = message.data.getBoolean(ARG_SHOW_NOTIFICATION)

            var allowedNetworkTypes = DownloadRequest.NETWORK_WIFI or DownloadRequest.NETWORK_MOBILE
            val appPreferences = ApplicationPreferences()
            if (appPreferences.isDownloadNetworkLimitedOnMobile) {
                allowedNetworkTypes = DownloadRequest.NETWORK_WIFI
            }

            val request = DownloadRequest.Builder()
                .url(url)
                .retryTime(1)
                .progressInterval(1, TimeUnit.SECONDS)
                .allowedNetworkTypes(allowedNetworkTypes)
                .destinationFilePath(filePath)
                .downloadCallback(object : DownloadCallback() {
                    override fun onStart(downloadId: Int, totalBytes: Long) {
                        if (Config.DEBUG) Log.i(TAG, "Download started: $url")

                        updateDownloadProgress(downloadId, 0, totalBytes)
                    }

                    override fun onProgress(downloadId: Int, bytesWritten: Long, totalBytes: Long) {
                        updateDownloadProgress(downloadId, bytesWritten, totalBytes)
                    }

                    override fun onSuccess(downloadId: Int, filePath: String?) {
                        if (Config.DEBUG) Log.i(TAG, "Download finished: $filePath")

                        updateDownloadSuccess(downloadId)

                        stopJob()
                    }

                    override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                        Log.e(TAG, "Download failed: $errMsg")

                        updateDownloadFailure(downloadId)

                        stopJob()
                    }
                })
                .build()

            downloadClient?.let { downloadClient ->
                val download = Download()
                download.title = title
                download.url = url
                download.filePath = filePath
                download.showNotification = showNotification
                download.id = downloadClient.add(request)

                downloadMap?.putIfAbsent(download.id, download)
            }
        }

    }

}
