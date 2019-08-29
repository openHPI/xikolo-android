package de.xikolo.testing.unit

import de.xikolo.utils.IdUtil
import org.junit.Assert.*
import org.junit.Test

class IdUtilTest {

    @Test
    fun testUUIDChecking() {
        assertTrue(
            IdUtil.isUUID("cedc3bbb-f383-49c4-abab-d4d56152864a")
        )

        assertFalse(
            IdUtil.isUUID("acedc3bbb-f383-49c4-abab-d4d56152864a")
        )

        assertFalse(
            IdUtil.isUUID("Cedc3bbb-f383-49c4-abab-d4d56152864a")
        )

        assertFalse(
            IdUtil.isUUID("cedc3bbbf38349c4ababd4d56152864a")
        )
    }

    @Test
    fun testConversion() {
        assertEquals(
            "cedc3bbb-f383-49c4-abab-d4d56152864a",
            IdUtil.base62ToUUID("6il46LhI9Bnpi7Dm1uyQUW")
        )
    }
}
