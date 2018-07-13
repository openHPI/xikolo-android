package de.xikolo.events

import de.xikolo.events.base.Event
import de.xikolo.models.AssetDownload

class DownloadStartedEvent(var download: AssetDownload) : Event(
    DownloadCompletedEvent::class.java.simpleName
        + ": filePath = "
        + download.filePath
        + ", type = "
        + download.title
)
