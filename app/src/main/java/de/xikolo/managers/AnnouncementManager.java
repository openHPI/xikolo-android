package de.xikolo.managers;

import de.xikolo.jobs.ListCourseAnnouncementsJob;
import de.xikolo.jobs.ListGlobalAnnouncementsJob;
import de.xikolo.jobs.UpdateAnnouncementVisitedJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Announcement;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class AnnouncementManager extends BaseManager {

    public static final String TAG = AnnouncementManager.class.getSimpleName();

    public RealmResults listGlobalAnnouncements(Realm realm, RealmChangeListener<RealmResults<Announcement>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Announcement> announcementListPromise = realm
                .where(Announcement.class)
                .findAllSortedAsync("publishedAt", Sort.DESCENDING);

        announcementListPromise.addChangeListener(listener);

        return announcementListPromise;
    }

    public RealmResults listCourseAnnouncements(String courseId, Realm realm, RealmChangeListener<RealmResults<Announcement>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Announcement> announcementListPromise = realm
                .where(Announcement.class)
                .equalTo("courseId", courseId)
                .findAllSortedAsync("publishedAt", Sort.DESCENDING);

        announcementListPromise.addChangeListener(listener);

        return announcementListPromise;
    }

    public void requestGlobalAnnouncementList(JobCallback callback) {
        jobManager.addJobInBackground(new ListGlobalAnnouncementsJob(callback));
    }

    public void requestCourseAnnouncementList(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new ListCourseAnnouncementsJob(courseId, callback));
    }

    public void updateAnnouncementVisited(String announcementId) {
        jobManager.addJobInBackground(new UpdateAnnouncementVisitedJob(announcementId));
    }

}
