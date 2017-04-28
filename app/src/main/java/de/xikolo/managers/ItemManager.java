package de.xikolo.managers;

import java.util.List;

import de.xikolo.managers.jobs.GetItemWithContentJob;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.managers.jobs.RetrieveVideoSubtitlesJob;
import de.xikolo.managers.jobs.UpdateItemVisitedJob;
import de.xikolo.models.Item;
import de.xikolo.models.Subtitle;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ItemManager extends BaseManager {

    public static final String TAG = ItemManager.class.getSimpleName();

    public RealmResults listItemsForSection(Realm realm, RealmChangeListener<RealmResults<Item>> listener, String sectionId) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Item> itemListPromise = realm
                .where(Item.class)
                .equalTo("sectionId", sectionId)
                .findAllSortedAsync("position");

        itemListPromise.addChangeListener(listener);

        return itemListPromise;
    }

    public void requestItemWithContent(JobCallback callback, String itemId) {
        jobManager.addJobInBackground(new GetItemWithContentJob(callback, itemId));
    }

    public void requestVideoSubtitles(Result<List<Subtitle>> result, String courseId, String moduleId, String videoId) {
        jobManager.addJobInBackground(new RetrieveVideoSubtitlesJob(result, courseId, moduleId, videoId));
    }

    public void updateItemVisited(String itemId) {
        jobManager.addJobInBackground(new UpdateItemVisitedJob(itemId));
    }

}
