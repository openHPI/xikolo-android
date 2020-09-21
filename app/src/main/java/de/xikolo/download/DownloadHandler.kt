package de.xikolo.download

interface DownloadHandler<I : DownloadIdentifier, R : DownloadRequest> {

    fun isDownloadingAnything(callback: (Boolean) -> Unit)

    fun download(
        request: R,
        listener: ((DownloadStatus?) -> Unit)? = null,
        callback: ((I?) -> Unit)? = null
    )

    fun cancel(
        identifier: I,
        callback: ((Boolean) -> Unit)? = null
    )

    fun status(
        identifier: I,
        callback: (DownloadStatus?) -> Unit
    )
}
