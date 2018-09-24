package de.xikolo.network.jobs


import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.network.sync.Sync
import de.xikolo.network.ApiService
import moe.banana.jsonapi2.HasOne
import ru.gildor.coroutines.retrofit.awaitResponse

class CreateEnrollmentJob(private val courseId: String, callback: RequestJobCallback) : RequestJob(callback, Precondition.AUTH) {

    companion object {
        val TAG: String = CreateEnrollmentJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val enrollment = Enrollment.JsonModel()
        enrollment.course = HasOne<Course.JsonModel>(Course.JsonModel().type, courseId)

        val response = ApiService.getInstance().createEnrollment(enrollment).awaitResponse()

        if (response.isSuccessful) {
            if (Config.DEBUG) Log.i(TAG, "Enrollment created")

            Sync.Data.with(Enrollment::class.java, response.body())
                    .saveOnly()
                    .setBeforeCommitCallback { realm, model ->
                        val course = realm.where(Course::class.java).equalTo("id", courseId).findFirst()
                        if (course != null) course.enrollmentId = model.id
                    }
                    .run()

            callback?.success()
        } else {
            if (Config.DEBUG) Log.w(TAG, "Enrollment not created")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
