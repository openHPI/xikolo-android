package de.xikolo.download

class DownloadStatus(
    var totalBytes: Long,
    var downloadedBytes: Long,
    var state: State
) {
    enum class State {
        PENDING, RUNNING, SUCCESSFUL, CANCELLED, FAILED;

        fun and(other: State): State {
            return if (this == FAILED || other == FAILED) {
                FAILED
            } else if (this == CANCELLED || other == CANCELLED) {
                CANCELLED
            } else if (this == RUNNING || other == RUNNING) {
                RUNNING
            } else if (this == PENDING || other == PENDING) {
                PENDING
            } else {
                SUCCESSFUL
            }
        }
    }
}
