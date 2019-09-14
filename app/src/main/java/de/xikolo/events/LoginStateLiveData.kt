package de.xikolo.events

import de.xikolo.models.LiveDataState

class LoginStateLiveData : LiveDataState<Boolean>() {

    fun loggedIn() {
        state(true)
    }

    fun loggedOut() {
        state(false)
    }
}
