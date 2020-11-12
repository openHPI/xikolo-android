package de.xikolo.download

import androidx.fragment.app.FragmentActivity

abstract class DownloadItem<out F, out I : DownloadIdentifier> {

    abstract val isDownloadable: Boolean

    abstract val downloadSize: Long

    abstract val title: String

    abstract val download: F?

    abstract val openAction: ((FragmentActivity) -> Unit)?

    abstract var stateListener: StateListener?

    abstract fun start(activity: FragmentActivity, callback: ((I?) -> Unit)? = null)

    abstract fun cancel(activity: FragmentActivity, callback: ((Boolean) -> Unit)? = null)

    abstract fun delete(activity: FragmentActivity, callback: ((Boolean) -> Unit)? = null)

    abstract fun getProgress(callback: (Pair<Long?, Long?>) -> Unit)

    abstract fun isDownloadRunning(callback: (Boolean) -> Unit)

    val downloadExists: Boolean
        get() {
            return download != null
        }

    interface StateListener {

        fun onStarted()

        fun onCompleted()

        fun onDeleted()
    }
}
