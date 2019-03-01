package de.xikolo.models

import com.squareup.moshi.Json
import de.xikolo.models.base.RealmAdapter
import de.xikolo.utils.DateUtil
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

open class CourseDate : RealmObject() {

    @PrimaryKey
    var id: String? = null

    var type: String? = null

    var title: String? = null

    var date: Date? = null

    @JsonApi(type = "course-dates")
    class JsonModel : Resource(), RealmAdapter<CourseDate> {

        @field:Json(name = "type")
        var dateType: String? = null

        var title: String? = null

        var date: String? = null

        override fun convertToRealmObject(): CourseDate {
            val courseDate = CourseDate()
            courseDate.id = id
            courseDate.title = title
            courseDate.date = DateUtil.parse(date)
            courseDate.type = dateType
            return courseDate
        }

    }

}
