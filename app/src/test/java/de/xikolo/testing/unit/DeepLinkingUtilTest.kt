package de.xikolo.testing.unit

import de.xikolo.controllers.helper.CourseArea
import de.xikolo.utils.DeepLinkingUtil
import org.junit.Assert.*
import org.junit.Test

class DeepLinkingUtilTest {

    @Test
    fun testCourseIdentification() {
        assertEquals(
            "123456",
            DeepLinkingUtil.getCourseIdentifier("/courses/123456")
        )

        assertEquals(
            "123456",
            DeepLinkingUtil.getCourseIdentifier("/courses/123456/resume")
        )

        assertNull(
            DeepLinkingUtil.getCourseIdentifier("/invalid")
        )
    }

    @Test
    fun testCourseTab() {
        assertTrue(
            CourseArea.LEARNINGS ==
                DeepLinkingUtil.getTab("/courses/123456/resume")
        )

        assertTrue(
            CourseArea.COURSE_DETAILS ==
                DeepLinkingUtil.getTab("/courses/123456")
        )

        assertTrue(
            CourseArea.COURSE_DETAILS ==
                DeepLinkingUtil.getTab("/courses/123456/invalid")
        )
    }

    @Test
    fun testAppArea() {
        assertEquals(
            DeepLinkingUtil.AppArea.ALL_COURSES,
            DeepLinkingUtil.getType("/courses")
        )

        assertEquals(
            DeepLinkingUtil.AppArea.MY_COURSES,
            DeepLinkingUtil.getType("/dashboard")
        )

        assertEquals(
            DeepLinkingUtil.AppArea.NEWS,
            DeepLinkingUtil.getType("/news")
        )

        assertNull(
            DeepLinkingUtil.getType("/invalid")
        )
    }
}
