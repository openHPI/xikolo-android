package de.xikolo.testing.unit

import de.xikolo.controllers.helper.MAX_SHORTCUTS
import de.xikolo.storages.RecentCourse
import de.xikolo.storages.RecentCoursesStorage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentCoursesTest {

    @Test
    fun testAddNewCourseToEmpty() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()
        assertTrue(set.size == 0)

        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        assertTrue(set.size == 1)
        testSet.add(Pair("id1", "Course1"))
        assertTrue(set == testSet)
    }

    @Test
    fun testAddNewCourseToPartlyFilled() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()
        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        testSet.add(Pair("id1", "Course1"))

        set = RecentCoursesStorage.addCourseToRecentCourses("id2", "Course2", set)
        testSet.add(Pair("id2", "Course2"))
        assertTrue(set == testSet)

        // check order
        assertTrue(set.last() == Pair("id2", "Course2"))
    }

    @Test
    fun testAddNewCourseToFull() {
        var set = LinkedHashSet<RecentCourse>()
        val fullTestSet = LinkedHashSet<RecentCourse>()

        (1..MAX_SHORTCUTS).forEach {
            set.add(Pair("id$it", "Course$it"))
            fullTestSet.add(Pair("id${it + 1}", "Course${it + 1}"))
        }

        set = RecentCoursesStorage.addCourseToRecentCourses("id5", "Course5", set)
        assertTrue(set.size == 4)
        assertFalse(set.contains(Pair("id1", "Course1")))
        assertTrue(set == fullTestSet)
    }

    @Test
    fun testAddDuplicateToEmpty() {
        var set = LinkedHashSet<RecentCourse>()
        val testSet = LinkedHashSet<RecentCourse>()

        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        set = RecentCoursesStorage.addCourseToRecentCourses("id1", "Course1", set)
        testSet.add(Pair("id1", "Course1"))
        assertTrue(set == testSet)
    }

    @Test
    fun testAddDuplicateToFull() {
        var set = LinkedHashSet<RecentCourse>()
        (1..MAX_SHORTCUTS).forEach { set.add(Pair("id$it", "Course$it")) }

        val fullTestSet = LinkedHashSet<RecentCourse>()
        fullTestSet.addAll(listOf(Pair("id1", "Course1"), Pair("id3", "Course3"), Pair("id4", "Course4"), Pair("id2", "Course2")))

        set = RecentCoursesStorage.addCourseToRecentCourses("id2", "Course2", set)
        assertTrue(set.size == 4)
        assertTrue(set.last() == Pair("id2", "Course2"))
        assertTrue(set == fullTestSet)
    }
}
