package de.xikolo.managers

import de.xikolo.network.jobs.ListCourseAnnouncementsJob
import de.xikolo.network.jobs.ListGlobalAnnouncementsJob
import de.xikolo.network.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.models.Announcement
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort

class AnnouncementManager {

    companion object {
        val TAG: String = AnnouncementManager::class.java.simpleName
    }

    fun listGlobalAnnouncements(realm: Realm, listener: RealmChangeListener<RealmResults<Announcement>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val announcementListPromise = realm
            .where(Announcement::class.java)
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()

        announcementListPromise.addChangeListener(listener)

        return announcementListPromise
    }

    fun listCourseAnnouncements(courseId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Announcement>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val announcementListPromise = realm
            .where(Announcement::class.java)
            .equalTo("courseId", courseId)
            .sort("publishedAt", Sort.DESCENDING)
            .findAllAsync()

        announcementListPromise.addChangeListener(listener)

        return announcementListPromise
    }

    fun requestGlobalAnnouncementList(callback: RequestJobCallback) {
        ListGlobalAnnouncementsJob(callback).run()
    }

    fun requestCourseAnnouncementList(courseId: String, callback: RequestJobCallback) {
        ListCourseAnnouncementsJob(courseId, callback).run()
    }

    fun updateAnnouncementVisited(announcementId: String) {
        UpdateAnnouncementVisitedJob.schedule(announcementId)
    }

}
