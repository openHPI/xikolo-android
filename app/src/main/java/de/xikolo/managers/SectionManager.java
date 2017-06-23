package de.xikolo.managers;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.jobs.ListSectionsWithItemsJob;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Section;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SectionManager extends BaseManager {

    public static final String TAG = SectionManager.class.getSimpleName();

    public RealmResults listSectionsForCourse(String courseId, Realm realm, RealmChangeListener<RealmResults<Section>> listener) {
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

    public Section getSection(String id, Realm realm, RealmChangeListener<Section> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        Section sectionPromise = realm
                .where(Section.class)
                .equalTo("courseId", id)
                .findFirstAsync();

        sectionPromise.addChangeListener(listener);

        return sectionPromise;
    }

    public void requestSectionListWithItems(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new ListSectionsWithItemsJob(courseId, callback));
    }

}
