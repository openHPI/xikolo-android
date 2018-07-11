package de.xikolo.lifecycle

import android.arch.lifecycle.LiveData
import de.xikolo.jobs.ListDocumentsWithLocalizationsForCourseJob
import de.xikolo.lifecycle.base.BaseViewModel
import de.xikolo.lifecycle.base.asLiveData
import de.xikolo.models.Document
import io.realm.RealmResults

open class DocumentsViewModel(val courseId: String) : BaseViewModel() {

    val documents: LiveData<RealmResults<Document>>
        get() = realm
            .where(Document::class.java)
            .findAllAsync()
            .asLiveData()

    override fun onCreate() {
        requestDocuments()
    }

    override fun onRefresh() {
        requestDocuments()
    }

    private fun requestDocuments() {
        ListDocumentsWithLocalizationsForCourseJob(courseId, networkState).run()
    }

}
