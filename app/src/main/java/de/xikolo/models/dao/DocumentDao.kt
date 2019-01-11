package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.kotlin.where

class DocumentDao(realm: Realm) : BaseDao(realm) {

    fun getDocumentsForCourse(courseId: String): LiveData<List<Document>> =
        // Workaround since Realm cannot query arrays of primitives yet
        // https://github.com/realm/realm-java/issues/5361
        MediatorLiveData<List<Document>>().also { mediator ->
            mediator.addSource(
                realm
                    .where<Document>()
                    .equalTo("isPublic", true)
                    .findAllAsync()
                    .asLiveData()
            ) { documents ->
                mediator.value = documents?.filter { it.courseIds.contains(courseId) }
            }
        }

    fun getLocalizations(): LiveData<List<DocumentLocalization>> =
        realm
            .where<DocumentLocalization>()
            .findAllAsync()
            .asLiveData()

}
