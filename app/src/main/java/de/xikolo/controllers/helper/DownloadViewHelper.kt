package de.xikolo.controllers.helper

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog
import de.xikolo.controllers.dialogs.ConfirmDeleteDialogAutoBundle
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

/**
 * When the url of the DownloadAsset's URL is null, the urlNotAvailableMessage is shown and the UI will be disabled.
 */
class DownloadViewHelper(
    private val activity: FragmentActivity,
    private val downloadAsset: DownloadAsset,
    title: CharSequence? = null,
    description: CharSequence? = null,
    urlNotAvailableMessage: CharSequence? = null
) {

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

    @BindView(R.id.textDescription)
    lateinit var textDescription: TextView

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

    init {
        val inflater = LayoutInflater.from(App.getInstance())
        view = inflater.inflate(R.layout.container_download, null)

        ButterKnife.bind(this, view)

        val appPreferences = ApplicationPreferences()

        buttonDownloadStart.setOnClickListener { _ ->
            if (NetworkUtil.isOnline()) {
                if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE && appPreferences.isDownloadNetworkLimitedOnMobile) {
                    val dialog = MobileDownloadDialog()
                    dialog.listener = object : MobileDownloadDialog.Listener {
                        override fun onDialogPositiveClick(dialog: DialogFragment) {
                            appPreferences.isDownloadNetworkLimitedOnMobile = false
                            startDownload()
                        }
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
                val dialog = ConfirmDeleteDialogAutoBundle.builder(false).build()
                dialog.listener = object : ConfirmDeleteDialog.Listener {
                    override fun onDialogPositiveClick(dialog: DialogFragment) {
                        deleteFile()
                    }

                    override fun onDialogPositiveAndAlwaysClick(dialog: DialogFragment) {
                        appPreferences.confirmBeforeDeleting = false
                        deleteFile()
                    }
                }
                dialog.show(activity.supportFragmentManager, ConfirmDeleteDialog.TAG)
            } else {
                deleteFile()
            }
        }

        if (title != null) {
            textFileName.text = title
        } else {
            textFileName.text = downloadAsset.title
        }

        if (description != null) {
            textDescription.text = description
            textDescription.visibility = View.VISIBLE
        } else {
            textDescription.visibility = View.GONE
        }

        if (downloadAsset.url == null) {
            view.isEnabled = false
            buttonDownloadStart.isEnabled = false

            if (urlNotAvailableMessage != null) {
                buttonDownloadStart.text = urlNotAvailableMessage
            } else {
                buttonDownloadStart.text = activity.getString(R.string.not_available)
            }
        }

        buttonOpenDownload.setOnClickListener { _ ->
            val file = downloadManager.getDownloadFile(downloadAsset)
            val target = Intent(Intent.ACTION_VIEW)
            target.setDataAndType(FileProviderUtil.getUriForFile(file), downloadAsset.mimeType)
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val intent = Intent.createChooser(target, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                App.getInstance().startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                ToastUtil.show(R.string.toast_no_file_viewer_found)
            }
        }

        EventBus.getDefault().register(this)

        progressBarUpdater = object : Runnable {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    if (progressBarUpdaterRunning) {

                        val bytesWritten = downloadManager.getDownloadWrittenBytes(downloadAsset)
                        val totalBytes = downloadManager.getDownloadTotalBytes(downloadAsset)

                        progressBarDownload.isIndeterminate = false
                        if (totalBytes == 0L) {
                            progressBarDownload.progress = 0
                        } else {
                            progressBarDownload.progress = (bytesWritten * 100 / totalBytes).toInt()
                        }
                        textFileSize.text = (FileUtil.getFormattedFileSize(bytesWritten) + " / "
                            + FileUtil.getFormattedFileSize(totalBytes))
                    }
                }

                if (progressBarUpdaterRunning) {
                    progressBarDownload.postDelayed(this, MILLISECONDS)
                }
            }
        }

        when {
            downloadManager.downloadRunningWithSecondaryAssets(downloadAsset) -> showRunningState()
            downloadManager.downloadExists(downloadAsset)                     -> showEndState()
            else                                                              -> showStartState()
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

        if (downloadAsset.sizeWithSecondaryAssets != 0L) {
            textFileSize.visibility = View.VISIBLE
            textFileSize.text = FileUtil.getFormattedFileSize(downloadAsset.sizeWithSecondaryAssets)
        } else {
            textFileSize.visibility = View.GONE
        }
    }

    private fun showRunningState() {
        viewDownloadStart.visibility = View.INVISIBLE
        viewDownloadRunning.visibility = View.VISIBLE
        viewDownloadEnd.visibility = View.INVISIBLE

        textFileSize.visibility = View.VISIBLE

        progressBarUpdaterRunning = true
        Thread(progressBarUpdater).start()
    }

    private fun showEndState() {
        viewDownloadStart.visibility = View.INVISIBLE
        viewDownloadRunning.visibility = View.INVISIBLE
        viewDownloadEnd.visibility = View.VISIBLE

        textFileSize.visibility = View.VISIBLE

        if (downloadAsset.sizeWithSecondaryAssets != 0L) {
            textFileSize.text = FileUtil.getFormattedFileSize(downloadAsset.sizeWithSecondaryAssets)
        } else {
            textFileSize.text = FileUtil.getFormattedFileSize(
                downloadManager.getDownloadFile(downloadAsset)
            )
        }

        progressBarUpdaterRunning = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadCompletedEvent(event: DownloadCompletedEvent) {
        if ((downloadAsset.url == event.url || downloadAsset.secondaryAssets.any { it.url == event.url })
            && !downloadManager.downloadRunningWithSecondaryAssets(downloadAsset)
            && downloadManager.downloadExists(downloadAsset)) {
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

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAllDownloadCancelledEvent(event: AllDownloadsCancelledEvent) {
        if (progressBarUpdaterRunning) {
            showStartState()
        }
    }

    fun onOpenFileClick(@StringRes buttonText: Int, onClick: () -> Unit) {
        buttonOpenDownload.text = activity.getString(buttonText)
        buttonOpenDownload.setOnClickListener { onClick.invoke() }
    }

}
