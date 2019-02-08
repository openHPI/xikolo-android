package de.xikolo.viewmodels

import androidx.lifecycle.LiveData
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.DocumentDao
import de.xikolo.network.jobs.ListDocumentsWithLocalizationsForCourseJob
import de.xikolo.viewmodels.base.BaseViewModel

open class DocumentListViewModel(val courseId: String) : BaseViewModel() {

    private val documentDao = DocumentDao(realm)

    val documentsForCourse: LiveData<List<Document>> by lazy {
        documentDao.getDocumentsForCourse(courseId)
    }

    val localizations: LiveData<List<DocumentLocalization>> by lazy {
        documentDao.getLocalizations()
    }

    override fun onFirstCreate() {
        requestDocuments(false)
    }

    override fun onRefresh() {
        requestDocuments(true)
    }

    private fun requestDocuments(userRequest: Boolean) {
        ListDocumentsWithLocalizationsForCourseJob(courseId, userRequest, networkState).run()
    }

}
