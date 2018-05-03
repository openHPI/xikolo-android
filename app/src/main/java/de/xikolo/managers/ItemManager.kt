package de.xikolo.managers

import de.xikolo.jobs.*
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Item
import de.xikolo.models.RichText
import de.xikolo.models.Video
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults

class ItemManager {

    companion object {
        val TAG: String = ItemManager::class.java.simpleName
    }

    fun listAccessibleItemsForSection(sectionId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Item>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val itemListPromise = realm
            .where(Item::class.java)
            .equalTo("sectionId", sectionId)
            .equalTo("accessible", true)
            .sort("position")
            .findAllAsync()

        itemListPromise.addChangeListener(listener)

        return itemListPromise
    }

    fun listAccessibleItemsForCourse(courseId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Item>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val itemListPromise = realm
            .where(Item::class.java)
            .equalTo("courseId", courseId)
            .equalTo("accessible", true)
            .findAllAsync()

        itemListPromise.addChangeListener(listener)

        return itemListPromise
    }

    fun getVideoForItem(contentId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Video>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        // RealmChangeListener for RealmObject doesn't notify for initial copyToRealm
        val videoPromise = realm
            .where(Video::class.java)
            .equalTo("id", contentId)
            .findAllAsync()

        videoPromise.addChangeListener(listener)

        return videoPromise
    }

    fun getRichTextForItem(contentId: String, realm: Realm, listener: RealmChangeListener<RealmResults<RichText>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        // RealmChangeListener for RealmObject doesn't notify for initial copyToRealm
        val richTextPromise = realm
            .where(RichText::class.java)
            .equalTo("id", contentId)
            .findAllAsync()

        richTextPromise.addChangeListener(listener)

        return richTextPromise
    }

    fun requestItemWithContent(itemId: String, callback: RequestJobCallback) {
        GetItemWithContentJob(callback, itemId).run()
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

    fun requestSectionListWithItems(courseId: String, callback: RequestJobCallback) {
        ListSectionsWithItemsJob(courseId, callback).run()
    }

    fun updateVideoProgress(video: Video, progress: Int, realm: Realm) {
        realm.executeTransaction {
            video.progress = progress
            it.copyToRealmOrUpdate(video)
        }
    }

}
