package de.xikolo.testing.instrumented.unit

import android.net.Uri
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.helper.CourseArea
import de.xikolo.testing.instrumented.mocking.base.BaseTest
import de.xikolo.utils.DeepLinkingUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkingUtilTest : BaseTest() {

    @Test
    fun testCourseIdentification() {
        assertEquals(
            "123456",
            DeepLinkingUtil.getCourseIdentifier(Uri.parse("https://open.hpi.de/courses/123456"))
        )

        assertEquals(
            "123456",
            DeepLinkingUtil.getCourseIdentifier(Uri.parse("https://open.hpi.de/courses/123456/resume"))
        )

        assertEquals(
            "123456",
            DeepLinkingUtil.getCourseIdentifier(Uri.parse("https://open.hpi.de/courses/123456/items/654321"))
        )

        if (FeatureConfig.RECAP_MODE) {
            assertEquals(
                "123456",
                DeepLinkingUtil.getCourseIdentifier(Uri.parse("https://open.hpi.de/learn?course_id=123456"))
            )
        }

        assertNull(
            DeepLinkingUtil.getCourseIdentifier(Uri.parse("https://open.hpi.de/invalid"))
        )
    }

    @Test
    fun testItemIdentification() {
        assertEquals(
            "654321",
            DeepLinkingUtil.getItemIdentifier("/courses/123456/items/654321")
        )

        assertNull(
            DeepLinkingUtil.getItemIdentifier("/courses/123456/items")
        )

        assertNull(
            DeepLinkingUtil.getItemIdentifier("/courses/items/654321")
        )
    }

    @Test
    fun testCourseTab() {
        assertEquals(
            CourseArea.LEARNINGS,
            DeepLinkingUtil.getTab("/courses/123456/resume")
        )

        if (FeatureConfig.RECAP_MODE) {
            assertEquals(
                CourseArea.RECAP,
                DeepLinkingUtil.getTab("/learn?course_id=123456")
            )
        }

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
