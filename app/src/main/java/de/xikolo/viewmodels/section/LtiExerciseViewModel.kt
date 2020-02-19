package de.xikolo.viewmodels.section

import de.xikolo.models.LtiExercise
import de.xikolo.models.dao.ItemDao
import de.xikolo.viewmodels.section.base.ItemViewModel

class LtiExerciseViewModel(itemId: String) : ItemViewModel(itemId) {

    val ltiExercise
        get() = ItemDao.Unmanaged.findContent(itemId) as LtiExercise?
}
