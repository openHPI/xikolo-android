package de.xikolo.viewmodels.course

import androidx.lifecycle.LiveData
import de.xikolo.models.Document
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.DocumentLocalizationDao
import de.xikolo.models.dao.DocumentDao
import de.xikolo.network.jobs.ListDocumentsWithLocalizationsForCourseJob
import de.xikolo.viewmodels.base.BaseViewModel

class DocumentListViewModel(val courseId: String) : BaseViewModel() {

    private val documentDao = DocumentDao(realm)
    private val localizationDao = DocumentLocalizationDao(realm)


    val documentsForCourse: LiveData<List<Document>> by lazy {
        documentDao.allForCourse(courseId)
    }

    val localizations: LiveData<List<DocumentLocalization>> by lazy {
        localizationDao.all()
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
