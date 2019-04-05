package de.xikolo.events


import de.xikolo.events.base.Event

class PermissionDeniedEvent(val requestCode: Int) : Event(
    "${PermissionDeniedEvent::class.java.simpleName}: id = $requestCode"
)
