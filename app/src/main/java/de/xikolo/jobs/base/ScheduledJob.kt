package de.xikolo.jobs.base

import com.evernote.android.job.Job
import de.xikolo.managers.UserManager
import java.io.IOException

abstract class ScheduledJob(private vararg val preconditions: Precondition) : Job() {

    @Throws(IOException::class)
    protected abstract fun onRun(params: Params): Result

    override fun onRunJob(params: Params): Result {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            return Result.FAILURE
        }

        return try {
            onRun(params)
        } catch (e: IOException) {
            Result.FAILURE
        }
    }

    enum class Precondition {
        AUTH
    }

}
