package de.xikolo.testing.unit

import de.xikolo.controllers.helper.CourseArea
import de.xikolo.utils.DeepLinkingUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertEquals(
            CourseArea.LEARNINGS,
            DeepLinkingUtil.getTab("/courses/123456/resume")
        )

        assertEquals(
            CourseArea.COURSE_DETAILS,
            DeepLinkingUtil.getTab("/courses/123456")
        )

        assertEquals(
            CourseArea.COURSE_DETAILS,
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
