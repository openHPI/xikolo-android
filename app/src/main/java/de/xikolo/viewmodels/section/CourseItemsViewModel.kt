package de.xikolo.viewmodels.section

import androidx.lifecycle.LiveData
import de.xikolo.managers.ItemManager
import de.xikolo.models.Item
import de.xikolo.models.Section
import de.xikolo.models.dao.SectionDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseDelegate
import de.xikolo.viewmodels.shared.SectionDelegate

class CourseItemsViewModel(courseId: String, sectionId: String) : BaseViewModel() {

    private val courseDelegate = CourseDelegate(realm, courseId)
    private val sectionDelegate = SectionDelegate(realm, courseId)

    private val sectionDao = SectionDao(realm)

    private val itemManager = ItemManager()

    val course = courseDelegate.course

    val section: LiveData<Section> by lazy {
        sectionDao.find(sectionId)
    }

    fun markItemVisited(item: Item) {
        realm.beginTransaction()
        item.visited = true
        realm.commitTransaction()

        itemManager.updateItemVisited(item.id)
    }

    override fun onFirstCreate() {
        courseDelegate.requestCourse(networkState, false)
        sectionDelegate.requestSectionListWithItems(networkState, false)
    }

    override fun onRefresh() {
        courseDelegate.requestCourse(networkState, true)
        sectionDelegate.requestSectionListWithItems(networkState, true)
    }

}
