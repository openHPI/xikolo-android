package de.xikolo.jobs

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.jobs.base.RequestJob
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.network.ApiService
import ru.gildor.coroutines.retrofit.awaitResponse

class CheckBirthdayVoucherJob(callback: RequestJobCallback) : RequestJob(callback) {

    companion object {
        val TAG: String = CheckBirthdayVoucherJob::class.java.simpleName
    }

    override suspend fun onRun() {
        val response = ApiService.getInstance().getBirthdayVoucher().awaitResponse()

        if (response.isSuccessful) {
            if (response.body()?.voucher == "awarded") {
                callback?.success()
            } else {
                callback?.error(RequestJobCallback.ErrorCode.ERROR)
            }
            if (Config.DEBUG) Log.i(TAG, "Birthday voucher: ${response.body()?.voucher}")
        } else {
            if (Config.DEBUG) Log.e(TAG, "Birthday voucher: ${response.code()} error")
            callback?.error(RequestJobCallback.ErrorCode.ERROR)
        }
    }

}
