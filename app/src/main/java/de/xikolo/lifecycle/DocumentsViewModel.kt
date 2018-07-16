package de.xikolo.lifecycle

import android.arch.lifecycle.LiveData
import de.xikolo.jobs.ListDocumentsWithLocalizationsForCourseJob
import de.xikolo.lifecycle.base.BaseViewModel
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.DocumentDao
import io.realm.RealmResults

open class DocumentsViewModel(val courseId: String) : BaseViewModel() {

    private val documentDao = DocumentDao(realm)

    fun getDocumentsForCourse(): LiveData<List<Document>> =
        documentDao.getDocumentsForCourse(courseId)

    fun getLocalizationsForDocument(documentId: String): LiveData<RealmResults<DocumentLocalization>> =
        documentDao.getLocalizationsForDocument(documentId)

    override fun onCreate() {
        requestDocuments()
    }

    override fun onRefresh() {
        requestDocuments(true)
    }

    private fun requestDocuments(userRequest: Boolean = false) {
        ListDocumentsWithLocalizationsForCourseJob(courseId, userRequest, networkState).run()
    }

}
