package de.xikolo.network.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.network.jobs.base.RequestJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.network.ApiService
import okhttp3.internal.http.HttpDate
import ru.gildor.coroutines.retrofit.awaitResponse

class CheckHealthJob(callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = CheckHealthJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().base().awaitResponse()

        when (response.code()) {
            in 200..299 -> {
                val apiVersionExpirationDate = response.headers().get(Config.HEADER_API_VERSION_EXPIRATION_DATE)
                if (apiVersionExpirationDate != null) {
                    if (Config.DEBUG) Log.e(TAG, "Health check: api deprecated and will expire at $apiVersionExpirationDate")
                    val expirationDate = HttpDate.parse(apiVersionExpirationDate)
                    callback?.deprecated(expirationDate)
                } else {
                    if (Config.DEBUG) Log.i(TAG, "Health check: successful")
                    callback?.success()
                }
            }
            406 -> {
                if (Config.DEBUG) Log.e(TAG, "Health check: api version expired")
                callback?.error(RequestJobCallback.ErrorCode.API_VERSION_EXPIRED)
            }
            503 -> {
                if (Config.DEBUG) Log.e(TAG, "Health check: server maintenance ongoing")
                callback?.error(RequestJobCallback.ErrorCode.MAINTENANCE)
            }
            else -> {
                if (Config.DEBUG) Log.e(TAG, "Health check: unclassified error")
                callback?.error(RequestJobCallback.ErrorCode.ERROR)
            }
        }
    }

}
