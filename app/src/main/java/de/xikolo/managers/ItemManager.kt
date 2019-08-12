package de.xikolo.managers

import de.xikolo.models.Video
import de.xikolo.network.jobs.UpdateItemVisitedJob
import io.realm.Realm

class ItemManager {

    companion object {
        val TAG: String = ItemManager::class.java.simpleName
    }

    fun updateItemVisited(itemId: String) {
        UpdateItemVisitedJob.schedule(itemId)
    }

    fun updateVideoProgress(video: Video, progress: Int, realm: Realm) {
        realm.executeTransaction {
            video.progress = progress
            it.copyToRealmOrUpdate(video)
        }
    }

}
