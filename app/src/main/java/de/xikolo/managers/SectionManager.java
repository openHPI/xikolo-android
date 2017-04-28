package de.xikolo.managers;

import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.managers.jobs.ListSectionsWithItemsJob;
import de.xikolo.models.Section;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SectionManager extends BaseManager {

    public static final String TAG = SectionManager.class.getSimpleName();

    public RealmResults listSectionsForCourse(Realm realm, RealmChangeListener<RealmResults<Section>> listener, String courseId) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Section> sectionListPromise = realm
                .where(Section.class)
                .equalTo("courseId", courseId)
                .findAllSortedAsync("position");

        sectionListPromise.addChangeListener(listener);

        return sectionListPromise;
    }

    public void requestSectionListWithItems(JobCallback callback, String courseId) {
        jobManager.addJobInBackground(new ListSectionsWithItemsJob(callback, courseId));
    }

}
