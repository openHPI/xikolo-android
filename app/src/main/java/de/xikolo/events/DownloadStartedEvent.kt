package de.xikolo.events

import de.xikolo.events.base.Event
import de.xikolo.utils.DownloadUtil

class DownloadStartedEvent(var download: DownloadUtil.AssetDownload) :
    Event(
        DownloadCompletedEvent::class.java.simpleName
            + ": filePath = "
            + download.filePath
            + ", type = "
            + download.title
    )
