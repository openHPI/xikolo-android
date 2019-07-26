package de.xikolo.viewmodels.section

import de.xikolo.models.RichText
import de.xikolo.models.dao.ItemDao
import de.xikolo.viewmodels.section.base.ItemViewModel

class RichTextViewModel(itemId: String) : ItemViewModel(itemId) {

    val richText
        get() = ItemDao.Unmanaged.findContent(itemId) as RichText?
}
