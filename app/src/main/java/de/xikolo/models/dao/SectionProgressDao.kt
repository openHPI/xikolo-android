package de.xikolo.models.dao

import de.xikolo.models.SectionProgress
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.Sort

class SectionProgressDao(realm: Realm) : BaseDao<SectionProgress>(SectionProgress::class, realm) {

    init {
        defaultSort = "position" to Sort.ASCENDING
    }

    fun allForCourse(courseId: String?) =
        all("courseProgressId" to courseId)

}
