package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.Item
import de.xikolo.models.dao.ItemsDao
import de.xikolo.network.jobs.ListSectionsWithItemsJob
import de.xikolo.viewmodels.base.BaseViewModel

open class LearningsViewModel(val courseId: String) : BaseViewModel() {

    private val itemsDao = ItemsDao(realm)
    private val courseViewModel = CourseViewModel(courseId)

    val course: LiveData<Course> = courseViewModel.course

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

