package de.xikolo.controllers.helper

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadViewHelper @JvmOverloads constructor(private val activity: FragmentActivity, private val downloadAsset: DownloadAsset, root: ViewGroup? = null) {

    companion object {
        val TAG: String = DownloadViewHelper::class.java.simpleName
        private const val MILLISECONDS = 250L
    }

    private val downloadManager: DownloadManager = DownloadManager(activity)

    val view: View

    @BindView(R.id.textFileName)
    lateinit var textFileName: TextView

    @BindView(R.id.textFileSize)
    lateinit var textFileSize: TextView

    @BindView(R.id.downloadStartContainer)
    lateinit var viewDownloadStart: View

    @BindView(R.id.buttonDownloadStart)
    lateinit var buttonDownloadStart: Button

    @BindView(R.id.downloadRunningContainer)
    lateinit var viewDownloadRunning: View

    @BindView(R.id.buttonDownloadCancel)
    lateinit var buttonDownloadCancel: TextView

    @BindView(R.id.progressDownload)
    lateinit var progressBarDownload: ProgressBar

    @BindView(R.id.downloadEndContainer)
    lateinit var viewDownloadEnd: View

    @BindView(R.id.buttonDownloadOpen)
    lateinit var buttonOpenDownload: Button

    @BindView(R.id.buttonDownloadDelete)
    lateinit var buttonDeleteDownload: Button

    private val progressBarUpdater: Runnable
    private var progressBarUpdaterRunning = false

    private var url: String? = null
    private var size: Int = 0

    init {
        val inflater = LayoutInflater.from(App.getInstance())
        view = inflater.inflate(R.layout.container_download, root)

        ButterKnife.bind(this, view)

        val appPreferences = ApplicationPreferences()

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

        buttonDownloadCancel.setOnClickListener { _ ->
            downloadManager.cancelAssetDownload(downloadAsset)
            showStartState()
        }

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

        textFileName.text = downloadAsset.title
        buttonOpenDownload.visibility = View.GONE

        when (downloadAsset) {
            is DownloadAsset.Course.Item -> {
                size = downloadAsset.size
                when (downloadAsset) {
                    is DownloadAsset.Course.Item.Slides -> {
                        textFileName.text = App.getInstance().getText(R.string.slides_as_pdf)
                        openFileAsPdf()
                    }
                    is DownloadAsset.Course.Item.Transcript -> {
                        textFileName.text = App.getInstance().getText(R.string.transcript_as_pdf)
                        openFileAsPdf()
                    }
                    is DownloadAsset.Course.Item.VideoHD -> {
                        textFileName.text = App.getInstance().getText(R.string.video_hd_as_mp4)
                    }
                    is DownloadAsset.Course.Item.VideoSD -> {
                        textFileName.text = App.getInstance().getText(R.string.video_sd_as_mp4)
                    }
                }
            }
            is DownloadAsset.Document -> {
                textFileName.text = downloadAsset.title
                openFileAsPdf()
            }
        }

        url = downloadAsset.url
        if (url == null) {
            view.visibility = View.GONE
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
                    progressBarDownload.postDelayed(this, MILLISECONDS)
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
        viewDownloadStart.visibility = View.VISIBLE
        viewDownloadRunning.visibility = View.INVISIBLE
        viewDownloadEnd.visibility = View.INVISIBLE

        progressBarDownload.progress = 0
        progressBarDownload.isIndeterminate = true
        progressBarUpdaterRunning = false

        textFileSize.text = FileUtil.getFormattedFileSize(size.toLong())
    }

    private fun showRunningState() {
        viewDownloadStart.visibility = View.INVISIBLE
        viewDownloadRunning.visibility = View.VISIBLE
        viewDownloadEnd.visibility = View.INVISIBLE

        progressBarUpdaterRunning = true
        Thread(progressBarUpdater).start()
    }

    private fun showEndState() {
        viewDownloadStart.visibility = View.INVISIBLE
        viewDownloadRunning.visibility = View.INVISIBLE
        viewDownloadEnd.visibility = View.VISIBLE

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
        buttonOpenDownload.visibility = View.VISIBLE
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
