package de.xikolo.viewmodels.section

import de.xikolo.models.Video
import de.xikolo.models.dao.ItemDao
import de.xikolo.viewmodels.section.base.ItemViewModel

class VideoPreviewViewModel(itemId: String) : ItemViewModel(itemId) {

    val video
        get() = ItemDao.Unmanaged.findContent(itemId) as Video?

}
