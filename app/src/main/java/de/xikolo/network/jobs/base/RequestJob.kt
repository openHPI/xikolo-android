package de.xikolo.network.jobs.base

import de.xikolo.managers.UserManager
import de.xikolo.models.*
import de.xikolo.network.sync.Sync
import de.xikolo.utils.NetworkUtil
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        GlobalScope.launch(Dispatchers.IO) {
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
        Sync.Included.with<RichText>(items)
                .saveOnly()
                .run()
        Sync.Included.with<Quiz>(items)
                .saveOnly()
                .run()
        Sync.Included.with<PeerAssessment>(items)
                .saveOnly()
                .run()
        Sync.Included.with<LtiExercise>(items)
                .saveOnly()
                .run()
        Sync.Included.with<Video>(items)
                .saveOnly()
                .setBeforeCommitCallback { realm, model ->
                    val localVideo = realm.where<Video>().equalTo("id", model.id).findFirst()
                    if (localVideo != null) model.progress = localVideo.progress
                }
                .run()
    }

    enum class Precondition {
        AUTH
    }

}
