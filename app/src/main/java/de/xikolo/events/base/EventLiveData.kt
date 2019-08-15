package de.xikolo.events.base

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class EventLiveData<T>(initialState: T) : LiveData<T>() {

    init {
        state(initialState)
    }

    fun state(state: T) {
        GlobalScope.launch(Dispatchers.Main) {
            value = state
        }
    }
}
