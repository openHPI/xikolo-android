package de.xikolo.utils

class MetaSectionList<H : Any, M : Any, S : List<Any>>(private var metaItem: M? = null, private var metaHeader: H? = null) {

    private val headers: MutableList<H> = mutableListOf()
    private val sections: MutableList<S> = mutableListOf()

    private val hasMetaItem = metaItem != null
    private val hasMetaHeader = metaHeader != null

    private val metaOffset = if (hasMetaItem) 1 else 0 + if (hasMetaHeader) 1 else 0

    val size
        get() = metaOffset + headers.size + sections.sumBy { it.size }

    fun clear() {
        metaItem = null
        metaHeader = null
        headers.clear()
        sections.clear()
    }

    fun add(header: H, section: S) {
        headers.add(header)
        sections.add(section)
    }

    fun getItem(position: Int): Any? {
        when {
            isMetaItem(position)   -> return metaItem
            isMetaHeader(position) -> return metaHeader
            else                   -> {
                var i = metaOffset
                var j = 0
                sections.forEach {
                    i += it.size + 1
                    if (position == i - it.size - 1) {
                        return headers[j]
                    } else if (position >= i - it.size && position < i) {
                        return sections[j][position - (i - it.size)]
                    }
                    j++
                }
            }
        }
        return null
    }

    fun isMetaItem(position: Int): Boolean {
        if (hasMetaItem
            && (hasMetaHeader && position == 1
                || !hasMetaHeader && position == 0)) {
            return true
        }
        return false
    }

    private fun isMetaHeader(position: Int): Boolean {
        return hasMetaHeader && position == 0
    }

    fun isHeader(position: Int): Boolean {
        if (isMetaHeader(position)) {
            return true
        }

        var i = metaOffset
        sections.forEach {
            i += it.size + 1
            if (position == i - it.size - 1) {
                return true
            }
        }

        return false
    }

    fun isSectionItem(position: Int): Boolean {
        return !isMetaItem(position) && !isHeader(position)
    }
}
