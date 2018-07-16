package de.xikolo.models

import com.squareup.moshi.Json
import de.xikolo.models.base.RealmAdapter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

open class Document : RealmObject() {

    @PrimaryKey
    var id: String = ""

    var description: String? = null

    var tags: RealmList<String> = RealmList()

    var isPublic: Boolean = false

    var courseIds: RealmList<String> = RealmList()

    @JsonApi(type = "documents")
    class JsonModel : Resource(), RealmAdapter<Document> {

        var description: String? = null

        var tags: List<String>? = null

        @Json(name = "public")
        var isPublic: Boolean = false

        var courses: HasMany<Course.JsonModel>? = null

        var courseIds: List<String>? = null
            get() = courses?.map { it.id }

        override fun convertToRealmObject(): Document {
            val model = Document()
            model.id = id
            model.description = description
            tags?.let { model.tags.addAll(it) }
            courseIds?.let { model.courseIds.addAll(it) }
            model.isPublic = isPublic
            return model
        }

    }

}
