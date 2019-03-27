package de.xikolo.network.jobs.base

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import de.xikolo.managers.UserManager
import kotlinx.coroutines.Dispatchers
import java.io.IOException

abstract class ScheduledJob(
    context: Context,
    params: WorkerParameters,
    private vararg val preconditions: Precondition
) : CoroutineWorker(context, params) {

    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork(): Result {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            return Result.failure()
        }

        return try {
            onRun(inputData)
        } catch (e: IOException) {
            Result.failure()
        }
    }

    @Throws(IOException::class)
    protected abstract suspend fun onRun(data: Data): Result

    enum class Precondition {
        AUTH
    }

}
