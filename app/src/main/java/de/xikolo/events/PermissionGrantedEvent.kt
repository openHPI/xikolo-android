package de.xikolo.events


import de.xikolo.events.base.Event

class PermissionGrantedEvent(val requestCode: Int) : Event(
    "${PermissionGrantedEvent::class.java.simpleName}: id = $requestCode"
)
