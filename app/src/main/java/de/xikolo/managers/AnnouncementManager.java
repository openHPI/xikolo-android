package de.xikolo.managers;

import de.xikolo.jobs.ListGlobalAnnouncementsJob;
import de.xikolo.jobs.UpdateAnnouncementVisitedJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;
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

    public Announcement getAnnouncement(String id, Realm realm, RealmChangeListener<Course> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        Announcement announcementPromise = realm
                .where(Announcement.class)
                .equalTo("id", id)
                .findFirstAsync();

        announcementPromise.addChangeListener(listener);

        return announcementPromise;
    }

    public void requestGlobalAnnouncementList(JobCallback callback) {
        jobManager.addJobInBackground(new ListGlobalAnnouncementsJob(callback));
    }

    public void updateAnnouncementVisited(String announcementId) {
        jobManager.addJobInBackground(new UpdateAnnouncementVisitedJob(announcementId));
    }

}
