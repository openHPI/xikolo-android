package de.xikolo.events

import de.xikolo.events.base.Event
import de.xikolo.models.DownloadAsset

class DownloadStartedEvent(var downloadAsset: DownloadAsset) : Event(
    DownloadCompletedEvent::class.java.simpleName
        + ": filePath = "
        + downloadAsset.filePath
        + ", type = "
        + downloadAsset.title
)
