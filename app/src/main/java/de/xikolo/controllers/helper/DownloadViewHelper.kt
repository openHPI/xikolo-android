package de.xikolo.controllers.helper

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog
import de.xikolo.controllers.dialogs.ConfirmDeleteDialogAutoBundle
import de.xikolo.controllers.dialogs.MobileDownloadDialog
import de.xikolo.extensions.observe
import de.xikolo.managers.DownloadManager
import de.xikolo.models.DownloadAsset
import de.xikolo.states.DownloadStateLiveData
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.FileProviderUtil
import de.xikolo.utils.extensions.*

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
        val inflater = LayoutInflater.from(App.instance)
        view = inflater.inflate(R.layout.item_download_helper, null)

        ButterKnife.bind(this, view)

        val appPreferences = ApplicationPreferences()

        buttonDownloadStart.setOnClickListener {
            if (activity.isOnline) {
                if (activity.connectivityType == ConnectivityType.CELLULAR && appPreferences.isDownloadNetworkLimitedOnMobile) {
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
                activity.showToast(R.string.toast_no_network)
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
                App.instance.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                activity.showToast(R.string.toast_no_file_viewer_found)
            }
        }

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
                        textFileSize.text = (bytesWritten.asFormattedFileSize + " / "
                            + totalBytes.asFormattedFileSize)
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

        registerDownloadStateObservers()
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
            textFileSize.text = downloadAsset.sizeWithSecondaryAssets.asFormattedFileSize
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

        textFileSize.text = if (downloadAsset.sizeWithSecondaryAssets != 0L) {
            downloadAsset.sizeWithSecondaryAssets.asFormattedFileSize
        } else {
            downloadManager.getDownloadFile(downloadAsset).fileSize.asFormattedFileSize
        }

        progressBarUpdaterRunning = false
    }

    private val wholeDownloadComplete
        get() = !downloadManager.downloadRunningWithSecondaryAssets(downloadAsset)
            && downloadManager.downloadExists(downloadAsset)


    private fun registerDownloadStateObservers() {
        // necessary because secondary download assets can complete after the main asset which would then not receive another event
        downloadAsset.secondaryAssets.forEach { asset ->
            App.instance.state.download.of(asset.url)
                .observe(activity) {
                    if (it == DownloadStateLiveData.DownloadStateCode.COMPLETED
                        && wholeDownloadComplete) {
                        showEndState()
                    }
                }
        }

        App.instance.state.download.of(downloadAsset.url)
            .observe(activity) {
                when (it) {
                    DownloadStateLiveData.DownloadStateCode.COMPLETED -> {
                        if (wholeDownloadComplete) {
                            showEndState()
                        }
                    }
                    DownloadStateLiveData.DownloadStateCode.STARTED   -> {
                        if (!progressBarUpdaterRunning) {
                            showRunningState()
                        }
                    }
                    DownloadStateLiveData.DownloadStateCode.DELETED   -> {
                        if (progressBarUpdaterRunning) {
                            showStartState()
                        }
                    }
                }
            }

        App.instance.state.downloadCancellation
            .observe(activity) {
                if (progressBarUpdaterRunning) {
                    showStartState()
                }
            }
    }

    fun onOpenFileClick(@StringRes buttonText: Int, onClick: () -> Unit) {
        buttonOpenDownload.text = activity.getString(buttonText)
        buttonOpenDownload.setOnClickListener { onClick.invoke() }
    }

}
