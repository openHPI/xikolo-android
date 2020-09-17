package de.xikolo.controllers.helper

import android.content.ActivityNotFoundException
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog
import de.xikolo.controllers.dialogs.ConfirmDeleteDialogAutoBundle
import de.xikolo.controllers.dialogs.MobileDownloadDialog
import de.xikolo.download.DownloadIdentifier
import de.xikolo.download.DownloadItem
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.ConnectivityType
import de.xikolo.utils.extensions.asFormattedFileSize
import de.xikolo.utils.extensions.connectivityType
import de.xikolo.utils.extensions.fileSize
import de.xikolo.utils.extensions.isOnline
import de.xikolo.utils.extensions.showToast
import java.io.File

/**
 * When the url of the DownloadAsset's URL is null, the urlNotAvailableMessage is shown and the UI will be disabled.
 */
class DownloadViewHelper(
    private val activity: FragmentActivity,
    private val download: DownloadItem<*, DownloadIdentifier>,
    title: CharSequence? = null,
    description: CharSequence? = null,
    urlNotAvailableMessage: CharSequence? = null
) {

    companion object {
        val TAG: String = DownloadViewHelper::class.java.simpleName
        private const val MILLISECONDS = 250L
    }

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
        view = activity.layoutInflater.inflate(R.layout.item_download_helper, null)

        ButterKnife.bind(this, view)

        val appPreferences = ApplicationPreferences()

        buttonDownloadStart.setOnClickListener {
            if (activity.isOnline) {
                if (activity.connectivityType == ConnectivityType.CELLULAR && appPreferences.isDownloadNetworkLimitedOnMobile) {
                    val dialog = MobileDownloadDialog()
                    dialog.listener = object : MobileDownloadDialog.MobileDownloadGrantedListener {
                        override fun onGranted(dialog: DialogFragment) {
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
            download.cancel(activity)
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
            textFileName.text = download.title
        }

        if (description != null) {
            textDescription.text = description
            textDescription.visibility = View.VISIBLE
        } else {
            textDescription.visibility = View.GONE
        }

        if (!download.isDownloadable) {
            view.isEnabled = false
            buttonDownloadStart.isEnabled = false

            if (urlNotAvailableMessage != null) {
                buttonDownloadStart.text = urlNotAvailableMessage
            } else {
                buttonDownloadStart.text = activity.getString(R.string.not_available)
            }
        }

        buttonOpenDownload.setOnClickListener {
            if (download.openIntent != null) {
                try {
                    activity.startActivity(download.openIntent)
                } catch (e: ActivityNotFoundException) {
                    activity.showToast(R.string.toast_no_file_viewer_found)
                }
            } else {
                activity.showToast(R.string.error_plain)
            }
        }

        progressBarUpdater = object : Runnable {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    download.getProgress { progress ->
                        if (progressBarUpdaterRunning) {
                            val downloadedBytes = progress.first ?: 0L
                            val totalBytes = progress.second ?: 0L

                            progressBarDownload.isIndeterminate = false
                            if (totalBytes == 0L) {
                                progressBarDownload.progress = 0
                            } else {
                                progressBarDownload.progress =
                                    (downloadedBytes * 100 / totalBytes).toInt()
                            }
                            textFileSize.text = activity.getString(
                                R.string.download_slash,
                                downloadedBytes.asFormattedFileSize,
                                totalBytes.asFormattedFileSize
                            )
                        }
                    }

                    if (progressBarUpdaterRunning) {
                        progressBarDownload.postDelayed(this, MILLISECONDS)
                    }
                }
            }
        }

        view.visibility = View.INVISIBLE
        download.isDownloadRunning {
            when {
                it -> showRunningState()
                download.downloadExists -> showEndState()
                else -> showStartState()
            }
            view.visibility = View.VISIBLE
        }

        registerDownloadStateListener()
    }

    private fun deleteFile() {
        download.delete(activity) {
            if (it) {
                showStartState()
            }
        }
    }

    private fun startDownload() {
        download.start(activity) {
            if (it != null) {
                showRunningState()
            }
        }
    }

    private fun showStartState() {
        viewDownloadStart.visibility = View.VISIBLE
        viewDownloadRunning.visibility = View.INVISIBLE
        viewDownloadEnd.visibility = View.INVISIBLE

        progressBarDownload.progress = 0
        progressBarDownload.isIndeterminate = true
        progressBarUpdaterRunning = false

        if (download.downloadSize != 0L) {
            textFileSize.visibility = View.VISIBLE
            textFileSize.text = download.downloadSize.asFormattedFileSize
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

        textFileSize.text = if (download.downloadSize != 0L) {
            download.downloadSize.asFormattedFileSize
        } else {
            (download.download as? File?).fileSize.asFormattedFileSize
        }

        progressBarUpdaterRunning = false
    }

    private fun registerDownloadStateListener() {
        download.stateListener = object : DownloadItem.StateListener {
            override fun onStarted() {
                if (!progressBarUpdaterRunning) {
                    showRunningState()
                }
            }

            override fun onCompleted() {
                if (download.downloadExists) {
                    showEndState()
                }
            }

            override fun onDeleted() {
                if (progressBarUpdaterRunning) {
                    showStartState()
                }
            }
        }
    }

    fun onOpenFileClick(@StringRes buttonText: Int, onClick: () -> Unit) {
        buttonOpenDownload.text = activity.getString(buttonText)
        buttonOpenDownload.setOnClickListener { onClick.invoke() }
    }
}
