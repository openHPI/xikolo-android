package de.xikolo.states.base

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class LiveDataEvent : LiveData<Int>() {

    private fun dispatchSignal() {
        GlobalScope.launch(Dispatchers.Main) {
            value = (value ?: 0) + 1
        }
    }

    open fun signal() {
        dispatchSignal()
    }
}
