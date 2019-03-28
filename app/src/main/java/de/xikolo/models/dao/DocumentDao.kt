package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.xikolo.extensions.asLiveData
import de.xikolo.models.Document
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm

class DocumentDao(realm: Realm) : BaseDao<Document>(Document::class, realm) {

    fun allForCourse(courseId: String?): LiveData<List<Document>> =
        // Workaround since Realm cannot query arrays of primitives yet
        // https://github.com/realm/realm-java/issues/5361
        MediatorLiveData<List<Document>>().also { mediator ->
            mediator.addSource(
                query()
                    .equalTo("isPublic", true)
                    .findAllAsync()
                    .asLiveData()
            ) { documents ->
                mediator.value = documents?.filter { it.courseIds.contains(courseId) }
            }
        }

}
