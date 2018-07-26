package de.xikolo.models

import com.squareup.moshi.Json
import de.xikolo.models.base.RealmAdapter
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

open class DocumentLocalization : RealmObject() {

    @PrimaryKey
    var id: String? = null

    var title: String? = null

    var description: String? = null

    var language: String? = null

    var revision: Int = 0

    var fileUrl: String? = null

    var documentId: String? = null

    @JsonApi(type = "document-localizations")
    open class JsonModel : Resource(), RealmAdapter<DocumentLocalization> {

        var title: String? = null

        var description: String? = null

        var language: String? = null

        var revision: Int = 0

        @Json(name = "file_url")
        var fileUrl: String? = null

        var document: HasOne<Document.JsonModel>? = null

        var documentId: String? = null
            get() = document?.get()?.id

        override fun convertToRealmObject(): DocumentLocalization {
            val model = DocumentLocalization()
            model.id = id
            model.title = title
            model.description = description
            model.language = language
            model.revision = revision
            model.fileUrl = fileUrl
            model.documentId = documentId
            return model
        }

    }

}
