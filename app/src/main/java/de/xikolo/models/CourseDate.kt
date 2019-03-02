package de.xikolo.models

import android.content.Context
import com.squareup.moshi.Json
import de.xikolo.R
import de.xikolo.models.base.RealmAdapter
import de.xikolo.utils.DateUtil
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

open class CourseDate : RealmObject() {

    @PrimaryKey
    var id: String? = null

    var type: String? = null

    var title: String? = null

    var date: Date? = null

    var courseId: String? = null

    fun getDisplayTypeString(context: Context): String {
        return when (type) {
            "course_start"               -> context.getString(R.string.course_date_type_course_start)
            "section_start"              -> context.getString(R.string.course_date_type_section_start)
            "item_submission_publishing" -> context.getString(R.string.course_date_type_submission_publishing)
            "item_submission_deadline"   -> context.getString(R.string.course_date_type_submission_deadline)
            else                         -> ""
        }
    }

    @JsonApi(type = "course-dates")
    class JsonModel : Resource(), RealmAdapter<CourseDate> {

        @field:Json(name = "type")
        var dateType: String? = null

        var title: String? = null

        var date: String? = null

        @Json(name = "courses")
        var course: HasOne<Enrollment.JsonModel>? = null

        override fun convertToRealmObject(): CourseDate {
            val courseDate = CourseDate()
            courseDate.id = id
            courseDate.title = title
            courseDate.date = DateUtil.parse(date)
            courseDate.type = dateType

            course?.let {
                courseDate.courseId = it.get().id
            }

            return courseDate
        }

    }

}
