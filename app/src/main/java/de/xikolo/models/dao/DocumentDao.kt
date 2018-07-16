package de.xikolo.models.dao

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import de.xikolo.lifecycle.base.asLiveData
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.RealmResults

class DocumentDao(realm: Realm) : BaseDao(realm) {

    fun getDocuments(): LiveData<RealmResults<Document>> =
        realm
            .where(Document::class.java)
            .findAllAsync()
            .asLiveData()

    fun getDocumentsForCourse(courseId: String): LiveData<List<Document>> =
        // Workaround since Realm cannot query arrays of primitives yet
        // https://github.com/realm/realm-java/issues/5361
        MediatorLiveData<List<Document>>().also { mediator ->
            mediator.addSource(
                realm
                    .where(Document::class.java)
                    .findAllAsync()
                    .asLiveData()
            ) { documents ->
                mediator.value = documents?.filter { it.courseIds.contains(courseId) }
            }
        }

    fun getLocalizationsForDocument(documentId: String): LiveData<RealmResults<DocumentLocalization>> =
        realm
            .where(DocumentLocalization::class.java)
            .equalTo("documentId", documentId)
            .findAllAsync()
            .asLiveData()

}
