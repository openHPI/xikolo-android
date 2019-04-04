package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.sync.Local
import ru.gildor.coroutines.retrofit.awaitResponse

class DeleteEnrollmentJob(private val id: String, callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = DeleteEnrollmentJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.instance.deleteEnrollment(id).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Enrollment deleted")

            Local.Delete.with(Enrollment::class.java, id)
                    .setBeforeCommitCallback { realm, model ->
                        val course = realm.where(Course::class.java).equalTo("enrollmentId", model.id).findFirst()
                        if (course != null) course.enrollmentId = null
                    }
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.w(TAG, "Enrollment not deleted")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
