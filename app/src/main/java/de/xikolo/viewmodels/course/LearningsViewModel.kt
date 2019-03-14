package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.Item
import de.xikolo.models.dao.CoursesDao
import de.xikolo.models.dao.ItemsDao
import de.xikolo.network.jobs.ListSectionsWithItemsJob
import de.xikolo.viewmodels.base.BaseViewModel

open class LearningsViewModel(val courseId: String) : BaseViewModel() {

    private val coursesDao = CoursesDao(realm)
    private val itemsDao = ItemsDao(realm)

    val course: LiveData<Course> by lazy {
        coursesDao.course(courseId)
    }

    val accessibleItems: LiveData<List<Item>> by lazy {
        itemsDao.accessibleItemsForCourse(courseId)
    }

    override fun onFirstCreate() {
        requestSectionListWithItems(false)
    }

    override fun onRefresh() {
        requestSectionListWithItems(true)
    }

    private fun requestSectionListWithItems(userRequest: Boolean) {
        ListSectionsWithItemsJob(courseId, networkState, userRequest).run()
    }
}

