package de.xikolo.events

import de.xikolo.events.base.Event

class NetworkStateEvent(val isOnline: Boolean) : Event(
    "${NetworkStateEvent::class.java.simpleName}: isOnline = $isOnline"
)
