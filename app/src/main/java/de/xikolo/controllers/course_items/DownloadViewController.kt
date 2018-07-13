package de.xikolo.controllers.course_items

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog
import de.xikolo.controllers.dialogs.MobileDownloadDialog
import de.xikolo.events.AllDownloadsCancelledEvent
import de.xikolo.events.DownloadCompletedEvent
import de.xikolo.events.DownloadDeletedEvent
import de.xikolo.events.DownloadStartedEvent
import de.xikolo.managers.DownloadManager
import de.xikolo.models.DownloadAsset
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.FileProviderUtil
import de.xikolo.utils.FileUtil
import de.xikolo.utils.NetworkUtil
import de.xikolo.utils.ToastUtil
import de.xikolo.views.IconButton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadViewController(private val activity: FragmentActivity, private val downloadAsset: DownloadAsset) {

    companion object {
        val TAG: String = DownloadViewController::class.java.simpleName
        private const val MILLISECONDS = 250
    }

    private val downloadManager: DownloadManager = DownloadManager(activity)

    val layout: View
    private val textFileName: TextView
    private val textFileSize: TextView
    private val viewDownloadStart: View?
    private val buttonDownloadStart: IconButton
    private val viewDownloadRunning: View?
    private val buttonDownloadCancel: TextView
    private val progressBarDownload: ProgressBar
    private val viewDownloadEnd: View?
    private val buttonOpenDownload: Button
    private val buttonDeleteDownload: Button

    private val progressBarUpdater: Runnable
    private var progressBarUpdaterRunning = false

    private var url: String? = null
    private var size: Int = 0

    init {
        val inflater = LayoutInflater.from(App.getInstance())
        layout = inflater.inflate(R.layout.container_download, null)

        textFileSize = layout.findViewById(R.id.textFileSize)
        textFileName = layout.findViewById(R.id.textFileName)

        val appPreferences = ApplicationPreferences()

        viewDownloadStart = layout.findViewById(R.id.downloadStartContainer)
        buttonDownloadStart = layout.findViewById(R.id.buttonDownloadStart)
        buttonDownloadStart.setOnClickListener { _ ->
            if (NetworkUtil.isOnline()) {
                if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE && appPreferences.isDownloadNetworkLimitedOnMobile) {
                    val dialog = MobileDownloadDialog.getInstance()
                    dialog.setMobileDownloadDialogListener { _ ->
                        appPreferences.isDownloadNetworkLimitedOnMobile = false
                        startDownload()
                    }
                    dialog.show(activity.supportFragmentManager, MobileDownloadDialog.TAG)
                } else {
                    startDownload()
                }
            } else {
                NetworkUtil.showNoConnectionToast()
            }
        }

        viewDownloadRunning = layout.findViewById(R.id.downloadRunningContainer)
        progressBarDownload = layout.findViewById(R.id.progressDownload)
        buttonDownloadCancel = layout.findViewById(R.id.buttonDownloadCancel)
        buttonDownloadCancel.setOnClickListener { _ ->
            downloadManager.cancelAssetDownload(downloadAsset)
            showStartState()
        }

        viewDownloadEnd = layout.findViewById(R.id.downloadEndContainer)
        buttonOpenDownload = layout.findViewById(R.id.buttonDownloadOpen)
        buttonDeleteDownload = layout.findViewById(R.id.buttonDownloadDelete)
        buttonDeleteDownload.setOnClickListener { _ ->
            if (appPreferences.confirmBeforeDeleting) {
                val dialog = ConfirmDeleteDialog.getInstance(false)
                dialog.setConfirmDeleteDialogListener(object : ConfirmDeleteDialog.ConfirmDeleteDialogListener {
                    override fun onDialogPositiveClick(dialog: DialogFragment) {
                        deleteFile()
                    }

                    override fun onDialogPositiveAndAlwaysClick(dialog: DialogFragment) {
                        appPreferences.confirmBeforeDeleting = false
                        deleteFile()
                    }
                })
                dialog.show(activity.supportFragmentManager, ConfirmDeleteDialog.TAG)
            } else {
                deleteFile()
            }
        }

        if (downloadAsset is DownloadAsset.Course.Item) {
            url = downloadAsset.url
            size = downloadAsset.size
            when (downloadAsset) {
                is DownloadAsset.Course.Item.Slides -> {
                    textFileName.text = App.getInstance().getText(R.string.slides_as_pdf)
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_pdf))
                    openFileAsPdf()
                }
                is DownloadAsset.Course.Item.Transcript -> {
                    textFileName.text = App.getInstance().getText(R.string.transcript_as_pdf)
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_pdf))
                    openFileAsPdf()
                }
                is DownloadAsset.Course.Item.VideoHD -> {
                    textFileName.text = App.getInstance().getText(R.string.video_hd_as_mp4)
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_video))
                    buttonOpenDownload.visibility = View.GONE
                }
                is DownloadAsset.Course.Item.VideoSD -> {
                    textFileName.text = App.getInstance().getText(R.string.video_sd_as_mp4)
                    buttonDownloadStart.setIconText(App.getInstance().getText(R.string.icon_download_video))
                    buttonOpenDownload.visibility = View.GONE
                }
            }
        }

        if (url == null) {
            layout.visibility = View.GONE
        }

        EventBus.getDefault().register(this)

        progressBarUpdater = object : Runnable {
            override fun run() {
                val dl = downloadManager.getDownload(downloadAsset)

                if (dl != null) {
                    Handler(Looper.getMainLooper()).post {
                        if (progressBarUpdaterRunning) {
                            progressBarDownload.isIndeterminate = false
                            if (dl.totalBytes == 0L) {
                                progressBarDownload.progress = 0
                            } else {
                                progressBarDownload.progress = (dl.bytesWritten * 100 / dl.totalBytes).toInt()
                            }
                            textFileSize.text = (FileUtil.getFormattedFileSize(dl.bytesWritten) + " / "
                                + FileUtil.getFormattedFileSize(dl.totalBytes))
                        }
                    }
                }

                if (progressBarUpdaterRunning) {
                    progressBarDownload.postDelayed(this, MILLISECONDS.toLong())
                }
            }
        }

        when {
            downloadManager.downloadRunning(downloadAsset)   -> showRunningState()
            downloadManager.downloadExists(downloadAsset)    -> showEndState()
            else                                             -> showStartState()
        }

    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this)
    }

    private fun deleteFile() {
        if (downloadManager.deleteAssetDownload(downloadAsset)) {
            showStartState()
        }
    }

    private fun startDownload() {
        if (downloadManager.startAssetDownload(downloadAsset)) {
            showRunningState()
        }
    }

    private fun showStartState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.visibility = View.VISIBLE
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.visibility = View.INVISIBLE
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.visibility = View.INVISIBLE
        }

        progressBarDownload.progress = 0
        progressBarDownload.isIndeterminate = true
        progressBarUpdaterRunning = false

        textFileSize.text = FileUtil.getFormattedFileSize(size.toLong())
    }

    private fun showRunningState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.visibility = View.INVISIBLE
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.visibility = View.VISIBLE
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.visibility = View.INVISIBLE
        }

        progressBarUpdaterRunning = true
        Thread(progressBarUpdater).start()
    }

    private fun showEndState() {
        if (viewDownloadStart != null) {
            viewDownloadStart.visibility = View.INVISIBLE
        }
        if (viewDownloadRunning != null) {
            viewDownloadRunning.visibility = View.INVISIBLE
        }
        if (viewDownloadEnd != null) {
            viewDownloadEnd.visibility = View.VISIBLE
        }

        textFileSize.text = FileUtil.getFormattedFileSize(size.toLong())

        progressBarUpdaterRunning = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadCompletedEvent(event: DownloadCompletedEvent) {
        if (event.url == url) {
            showEndState()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadStartedEvent(event: DownloadStartedEvent) {
        if (event.downloadAsset == downloadAsset && !progressBarUpdaterRunning) {
            showRunningState()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadDeletedEvent(event: DownloadDeletedEvent) {
        if (event.downloadAsset == downloadAsset && progressBarUpdaterRunning) {
            showStartState()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAllDownloadCancelledEvent(event: AllDownloadsCancelledEvent) {
        if (progressBarUpdaterRunning) {
            showStartState()
        }
    }

    private fun openFileAsPdf() {
        buttonOpenDownload.text = App.getInstance().resources.getText(R.string.open)
        buttonOpenDownload.setOnClickListener { _ ->
            val pdf = downloadManager.getDownloadFile(downloadAsset)
            val target = Intent(Intent.ACTION_VIEW)
            target.setDataAndType(FileProviderUtil.getUriForFile(pdf), "application/pdf")
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val intent = Intent.createChooser(target, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                App.getInstance().startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                ToastUtil.show(R.string.toast_no_pdf_viewer_found)
            }
        }
    }

}
