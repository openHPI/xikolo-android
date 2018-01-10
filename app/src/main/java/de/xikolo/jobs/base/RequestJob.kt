package de.xikolo.jobs.base

import de.xikolo.managers.UserManager
import de.xikolo.models.*
import de.xikolo.models.base.Sync
import de.xikolo.utils.NetworkUtil
import kotlinx.coroutines.experimental.launch

abstract class RequestJob(protected val callback: RequestJobCallback?, private vararg val preconditions: Precondition) {

    fun run() {
        if (preconditions.contains(Precondition.AUTH) && !UserManager.isAuthorized) {
            callback?.error(RequestJobCallback.ErrorCode.NO_AUTH)
            return
        }

        if (!NetworkUtil.isOnline()) {
            callback?.error(RequestJobCallback.ErrorCode.NO_NETWORK)
            return
        }

        launch {
            try {
                onRun()
            } catch (e: Throwable) {
                callback?.error(RequestJobCallback.ErrorCode.ERROR)
            }
        }
    }

    protected abstract suspend fun onRun()

    protected fun syncItemContent(item: Item.JsonModel) {
        syncItemContent(arrayOf(item))
    }

    protected fun syncItemContent(items: Array<Item.JsonModel>) {
        Sync.Included.with(RichText::class.java, *items)
                .saveOnly()
                .run()
        Sync.Included.with(Quiz::class.java, *items)
                .saveOnly()
                .run()
        Sync.Included.with(PeerAssessment::class.java, *items)
                .saveOnly()
                .run()
        Sync.Included.with(LtiExercise::class.java, *items)
                .saveOnly()
                .run()
        Sync.Included.with(Video::class.java, *items)
                .saveOnly()
                .setBeforeCommitCallback { realm, model ->
                    val localVideo = realm.where(Video::class.java).equalTo("id", model.id).findFirst()
                    if (localVideo != null) model.progress = localVideo.progress
                }
                .run()
    }

    enum class Precondition {
        AUTH
    }

}
