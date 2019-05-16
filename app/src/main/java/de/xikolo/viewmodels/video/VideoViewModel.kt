package de.xikolo.viewmodels.video

import androidx.lifecycle.LiveData
import de.xikolo.models.Item
import de.xikolo.models.Video
import de.xikolo.models.dao.ItemDao
import de.xikolo.models.dao.VideoDao
import de.xikolo.viewmodels.base.BaseViewModel

class VideoViewModel(val courseId: String, val sectionId: String, val itemId: String, val videoId: String) : BaseViewModel() {

    private val itemDao = ItemDao(realm)
    private val videoDao = VideoDao(realm)

    val item: LiveData<Item> by lazy {
        itemDao.find(itemId)
    }

    val video: LiveData<Video> by lazy {
        videoDao.find(videoId)
    }

    override fun onRefresh() {
        // ToDo refactor this when Items have been changed to ViewModel
    }
}
