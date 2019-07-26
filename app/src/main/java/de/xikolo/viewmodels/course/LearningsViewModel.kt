package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Item
import de.xikolo.models.dao.ItemDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.SectionDelegate

open class LearningsViewModel(private val courseId: String) : BaseViewModel() {

    private val sectionDelegate = SectionDelegate(realm, courseId)

    private val itemsDao = ItemDao(realm)

    val accessibleItems: LiveData<List<Item>> by lazy {
        itemsDao.allAccessibleForCourse(courseId)
    }

    override fun onFirstCreate() {
        sectionDelegate.requestSectionListWithItems(networkState, false)
    }

    override fun onRefresh() {
        sectionDelegate.requestSectionListWithItems(networkState, true)
    }
}

