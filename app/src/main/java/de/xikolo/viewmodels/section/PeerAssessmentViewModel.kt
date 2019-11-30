package de.xikolo.viewmodels.section

import de.xikolo.models.PeerAssessment
import de.xikolo.models.dao.ItemDao
import de.xikolo.viewmodels.section.base.ItemViewModel

class PeerAssessmentViewModel(itemId: String) : ItemViewModel(itemId) {

    val peerAssessment
        get() = ItemDao.Unmanaged.findContent(itemId) as PeerAssessment?
}
