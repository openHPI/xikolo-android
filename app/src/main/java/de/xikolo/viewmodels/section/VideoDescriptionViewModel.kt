package de.xikolo.viewmodels.section

import androidx.lifecycle.LiveData
import de.xikolo.models.Video
import de.xikolo.models.dao.VideoDao
import de.xikolo.viewmodels.section.base.ItemViewModel

class VideoDescriptionViewModel(itemId: String, val videoId: String) : ItemViewModel(itemId) {

    private val videoDao = VideoDao(realm)

    val video: LiveData<Video> by lazy {
        videoDao.find(videoId)
    }
}
