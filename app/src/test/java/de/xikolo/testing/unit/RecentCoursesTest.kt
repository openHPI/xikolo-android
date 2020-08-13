package de.xikolo.testing.unit

import de.xikolo.storages.RecentCoursesStorage
import de.xikolo.utils.ShortcutUtil.MAX_SHORTCUTS
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

typealias RecentCourse = Pair<String, String>

class RecentCoursesTest {

    @Test
    fun testAddNewCourseToEmpty() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()
        assertTrue(set.size == 0)

        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        assertTrue(set.size == 1)
        testSet.add(RecentCourse("id1", "Course1"))
        assertTrue(set == testSet)
    }

    @Test
    fun testAddNewCourseToPartlyFilled() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()
        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        testSet.add(RecentCourse("id1", "Course1"))

        set = RecentCoursesStorage.addCourseToRecentCourses("id2", "Course2", set)
        testSet.add(RecentCourse("id2", "Course2"))
        assertTrue(set == testSet)

        // check order
        assertTrue(set.last() == RecentCourse("id2", "Course2"))
    }

    @Test
    fun testAddNewCourseToFull() {
        var set = LinkedHashSet<RecentCourse>()
        val fullTestSet = LinkedHashSet<RecentCourse>()

        (1..MAX_SHORTCUTS).forEach {
            set.add(RecentCourse("id$it", "Course$it"))
            fullTestSet.add(RecentCourse("id${it + 1}", "Course${it + 1}"))
        }

        set = RecentCoursesStorage.addCourseToRecentCourses("id5", "Course5", set)
        assertTrue(set.size == MAX_SHORTCUTS)
        assertFalse(set.contains(RecentCourse("id1", "Course1")))
        assertTrue(set == fullTestSet)
    }

    @Test
    fun testAddDuplicateToEmpty() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()

        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        testSet.add(RecentCourse("id1", "Course1"))
        assertTrue(set == testSet)
    }

    @Test
    fun testAddDuplicateToFull() {
        var set = LinkedHashSet<RecentCourse>()
        (1..MAX_SHORTCUTS).forEach { set.add(RecentCourse("id$it", "Course$it")) }

        val fullTestSet = LinkedHashSet<RecentCourse>()
        fullTestSet.addAll(listOf(RecentCourse("id1", "Course1"), RecentCourse("id3", "Course3"), RecentCourse("id4", "Course4"), RecentCourse("id2", "Course2")))

        set = RecentCoursesStorage.addCourseToRecentCourses("id2", "Course2", set)
        assertTrue(set.size == MAX_SHORTCUTS)
        assertTrue(set.last() == RecentCourse("id2", "Course2"))
        assertTrue(set == fullTestSet)
    }
}
