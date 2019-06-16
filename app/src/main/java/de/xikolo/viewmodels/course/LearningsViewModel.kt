package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Item
import de.xikolo.models.dao.ItemDao
import de.xikolo.network.jobs.ListSectionsWithItemsJob
import de.xikolo.viewmodels.base.BaseViewModel

open class LearningsViewModel(private val courseId: String) : BaseViewModel() {

    private val itemsDao = ItemDao(realm)

    val accessibleItems: LiveData<List<Item>> by lazy {
        itemsDao.allAccessibleForCourse(courseId)
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

