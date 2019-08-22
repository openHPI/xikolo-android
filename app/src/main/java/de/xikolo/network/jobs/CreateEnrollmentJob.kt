package de.xikolo.network.jobs


import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.network.ApiService
import de.xikolo.network.jobs.base.NetworkJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.network.sync.Sync
import io.realm.kotlin.where
import moe.banana.jsonapi2.HasOne
import ru.gildor.coroutines.retrofit.awaitResponse

class CreateEnrollmentJob(private val courseId: String, networkState: NetworkStateLiveData, userRequest: Boolean) : NetworkJob(networkState, userRequest, Precondition.AUTH) {

    companion object {
        val TAG: String = CreateEnrollmentJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val enrollment = Enrollment.JsonModel()
        enrollment.course = HasOne<Course.JsonModel>(Course.JsonModel().type, courseId)

        val response = ApiService.instance.createEnrollment(enrollment).awaitResponse()

        if (response.isSuccessful && response.body() != null) {
            if (Config.DEBUG) Log.i(TAG, "Enrollment created")

            Sync.Data.with(response.body()!!)
                .saveOnly()
                .setBeforeCommitCallback { realm, model ->
                    realm.where<Course>()
                        .equalTo("id", courseId)
                        .findFirst()
                        ?.enrollmentId = model.id
                }
                .run()

            success()
        } else {
            if (Config.DEBUG) Log.w(TAG, "Enrollment not created")
            error()
        }
    }

}
