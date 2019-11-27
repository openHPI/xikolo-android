package de.xikolo.models

import de.xikolo.App
import de.xikolo.R
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

class Ticket {

    @JsonApi(type = "tickets")
    class JsonModel : Resource() {
        var title: String? = null
        var report: String? = null
        var topic: String? = null
        var language: String? = null
        var data: String? = null
        var mail: String? = null
        var course: HasOne<Course.JsonModel>? = null

    }
}

enum class TicketTopic(val apiTitle: String) {
    TECHNICAL("technical"),
    COURSE("course"),
    REACTIVATION("reactivation"),
    NONE("none");

    override fun toString(): String {
        return when (this) {
            TECHNICAL    -> App.instance.getString(R.string.helpdesk_topic_list_technical_question)
            REACTIVATION -> App.instance.getString(R.string.helpdesk_topic_list_reactivation)
            else         -> apiTitle
        }
    }

}
