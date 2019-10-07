package de.xikolo.states

import de.xikolo.states.base.LiveDataState

class LoginStateLiveData : LiveDataState<Boolean>() {

    fun loggedIn() {
        state(true)
    }

    fun loggedOut() {
        state(false)
    }
}
