package de.xikolo.events

import de.xikolo.events.base.Event

class DownloadCompletedEvent(val url: String) : Event(
    "${DownloadCompletedEvent::class.java.simpleName}: url = $url"
)
