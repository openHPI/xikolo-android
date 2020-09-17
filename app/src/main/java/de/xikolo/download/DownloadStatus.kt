package de.xikolo.download

class DownloadStatus(
    var totalBytes: Long,
    var downloadedBytes: Long,
    var state: State
) {
    enum class State {
        PENDING, RUNNING, SUCCESSFUL, CANCELLED, FAILED;

        // Determines what two states result in together.
        fun and(other: State): State {
            return when {
                this == FAILED || other == FAILED -> FAILED
                this == CANCELLED || other == CANCELLED -> CANCELLED
                this == RUNNING || other == RUNNING -> RUNNING
                this == PENDING || other == PENDING -> PENDING
                else -> SUCCESSFUL
            }
        }
    }
}
