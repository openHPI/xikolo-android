package de.xikolo.events

import de.xikolo.events.base.Event
import de.xikolo.models.DownloadAsset

class DownloadDeletedEvent(val downloadAsset: DownloadAsset) : Event(
    "${DownloadDeletedEvent::class.java.simpleName}: filePath = ${downloadAsset.filePath}, type = ${downloadAsset.title}"
)
