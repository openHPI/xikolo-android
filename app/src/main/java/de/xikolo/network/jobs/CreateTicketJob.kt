package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Course
import de.xikolo.models.Ticket
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import moe.banana.jsonapi2.HasOne
import ru.gildor.coroutines.retrofit.awaitResponse
import java.util.*

class CreateTicketJob(val title: String, private val report: String, private val topic: String, private val mail: String? = null, val courseId: String? = null, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest) {

    companion object {
        val TAG: String = CreateTicketJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val ticket = Ticket.JsonModel()
        ticket.title = title
        ticket.report = report
        ticket.topic = topic
        ticket.language = Locale.getDefault().language
        ticket.data = Config.HEADER_USER_AGENT_VALUE
        ticket.mail = mail

        courseId?.let { ticket.course = HasOne(Course.JsonModel().type, it) }

        val response = ApiService.instance.createTicket(ticket).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Ticket created")
            success()
        } else {
            error()
        }
    }

}
