package de.xikolo.testing.unit

import de.xikolo.utils.MetaSectionList
import junit.framework.TestCase
import org.junit.Test

class MetaSectionListTest : TestCase() {

    private lateinit var list: MetaSectionList<String, String, List<String>>

    @Test
    fun testSimpleList() {
        list = MetaSectionList()

        list.add("header", listOf("item1", "item2", "item3"))

        assertTrue(list.size == 4)
        assertTrue(list.get(0) == "header")
        assertTrue(list.isHeader(0))
        assertFalse(list.isSectionItem(0))
        assertTrue(list.isSectionItem(1))

        list.add("another header", listOf())

        assertTrue(list.size == 5)

        list.clear()

        assertTrue(list.size == 0)

        list.add(null, listOf("item"))

        assertTrue(list.size == 2)
    }

    @Test
    fun testMetaList() {
        list = MetaSectionList("meta item")

        assertTrue(list.size == 1)

        list.add("header", listOf("item1", "item2", "item3"))

        assertTrue(list.size == 5)
        assertTrue(list.isMetaItem(0))
        assertFalse(list.isHeader(0))
        assertFalse(list.isSectionItem(0))

        assertTrue(list.get(1) == "header")
        assertTrue(list.isHeader(1))

        list.add("another header", listOf())

        assertTrue(list.size == 6)

        list.clear()

        assertTrue(list.size == 1)

        list.add(null, listOf("item"))

        assertTrue(list.size == 3)
    }

    @Test
    fun testMetaHeaderList() {
        list = MetaSectionList("meta item", "meta header")

        assertTrue(list.size == 2)

        list.add("header", listOf("item1", "item2", "item3"))

        assertTrue(list.size == 6)
        assertTrue(list.isMetaItem(1))
        assertTrue(list.isHeader(0))
        assertFalse(list.isSectionItem(1))

        assertTrue(list.get(2) == "header")
        assertTrue(list.isHeader(2))

        list.add("another header", listOf())

        assertTrue(list.size == 7)

        list.clear()

        assertTrue(list.size == 2)

        list.add(null, listOf("item"))

        assertTrue(list.size == 4)
    }
}
