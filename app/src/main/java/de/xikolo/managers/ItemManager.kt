package de.xikolo.managers

import de.xikolo.models.Video
import de.xikolo.network.jobs.ListItemsWithContentForSectionJob
import de.xikolo.network.jobs.ListSubtitlesWithCuesJob
import de.xikolo.network.jobs.UpdateItemVisitedJob
import de.xikolo.network.jobs.base.RequestJobCallback
import io.realm.Realm

class ItemManager {

    companion object {
        val TAG: String = ItemManager::class.java.simpleName
    }

    fun requestItemsWithContentForSection(sectionId: String, callback: RequestJobCallback) {
        ListItemsWithContentForSectionJob(callback, sectionId).run()
    }

    fun requestSubtitlesWithCuesForVideo(videoId: String, callback: RequestJobCallback) {
        ListSubtitlesWithCuesJob(callback, videoId).run()
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
