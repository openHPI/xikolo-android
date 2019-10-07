package de.xikolo.states

import de.xikolo.states.base.LiveDataState

class PermissionStateLiveData : LiveDataState<PermissionStateLiveData.PermissionStateCode>() {

    companion object {
        private val map = mutableMapOf<Int, PermissionStateLiveData>()

        fun of(key: Int): PermissionStateLiveData {
            val newObj = PermissionStateLiveData()
            if (!map.containsKey(key)) {
                map[key] = newObj
            }

            return map[key] ?: newObj
        }
    }

    fun granted() {
        super.state(PermissionStateCode.GRANTED)
    }

    fun denied() {
        super.state(PermissionStateCode.DENIED)
    }

    enum class PermissionStateCode {
        GRANTED, DENIED
    }
}
