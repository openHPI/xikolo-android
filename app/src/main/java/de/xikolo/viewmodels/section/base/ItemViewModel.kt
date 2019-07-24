package de.xikolo.viewmodels.section.base

import androidx.lifecycle.LiveData
import de.xikolo.models.Item
import de.xikolo.models.dao.ItemDao
import de.xikolo.network.jobs.GetItemWithContentJob
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ItemViewModel(val itemId: String) : BaseViewModel() {

    private val itemDao = ItemDao(realm)

    val item: LiveData<Item> by lazy {
        itemDao.find(itemId)
    }

    override fun onFirstCreate() {
        requestItem(false)
    }

    override fun onRefresh() {
        requestItem(true)
    }

    private fun requestItem(userRequest: Boolean) {
        GetItemWithContentJob(itemId, networkState, userRequest).run()
    }

}