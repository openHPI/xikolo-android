package de.xikolo.events

import de.xikolo.events.base.Event
import de.xikolo.models.DownloadAsset

class DownloadStartedEvent(val downloadAsset: DownloadAsset) : Event(
    "${DownloadStartedEvent::class.java.simpleName}: filePath = ${downloadAsset.filePath}, type = ${downloadAsset.title}"
)
