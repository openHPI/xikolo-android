package de.xikolo.events

import de.xikolo.events.base.EventLiveData

class ConnectivityEventLiveData(initialState: Boolean) : EventLiveData<Boolean>(initialState) {

    fun online() {
        state(true)
    }

    fun offline() {
        state(false)
    }
}
