package de.xikolo.states

import de.xikolo.states.base.LiveDataState

class DownloadStateLiveData : LiveDataState<DownloadStateLiveData.DownloadStateCode>(DownloadStateCode.DELETED) {

    companion object {
        private val map = mutableMapOf<String, DownloadStateLiveData>()

        fun of(key: String?): DownloadStateLiveData {
            return if (key != null) {
                val newObj = DownloadStateLiveData()
                if (!map.containsKey(key)) {
                    map[key] = newObj
                }

                map[key] ?: newObj
            } else {
                DownloadStateLiveData()
            }
        }
    }

    fun running() {
        super.state(DownloadStateCode.STARTED)
    }

    fun deleted() {
        super.state(DownloadStateCode.DELETED)
    }

    fun completed() {
        super.state(DownloadStateCode.COMPLETED)
    }

    enum class DownloadStateCode {
        STARTED, COMPLETED, DELETED
    }

}
