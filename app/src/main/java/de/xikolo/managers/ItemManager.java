package de.xikolo.managers;

import java.util.List;

import de.xikolo.managers.jobs.GetItemWithContentJob;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.managers.jobs.ListItemsWithContentForSectionJob;
import de.xikolo.managers.jobs.RetrieveVideoSubtitlesJob;
import de.xikolo.managers.jobs.UpdateItemVisitedJob;
import de.xikolo.models.Item;
import de.xikolo.models.Subtitle;
import de.xikolo.models.Video;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ItemManager extends BaseManager {

    public static final String TAG = ItemManager.class.getSimpleName();

    public RealmResults listItemsForSection(String sectionId, Realm realm, RealmChangeListener<RealmResults<Item>> listener) {
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

    public Video getVideoForItem(String itemId, Realm realm, RealmChangeListener<Video> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        Video videoPromise = realm
                .where(Video.class)
                .equalTo("itemId", itemId)
                .findFirstAsync();

        videoPromise.addChangeListener(listener);

        return videoPromise;
    }

    public void requestItemWithContent(String itemId, JobCallback callback) {
        jobManager.addJobInBackground(new GetItemWithContentJob(callback, itemId));
    }

    public void requestItemsWithContentForSection(String sectionId, JobCallback callback) {
        jobManager.addJobInBackground(new ListItemsWithContentForSectionJob(callback, sectionId));
    }

    public void requestVideoSubtitles(String courseId, String sectionId, String videoId, Result<List<Subtitle>> result) {
        jobManager.addJobInBackground(new RetrieveVideoSubtitlesJob(result, courseId, sectionId, videoId));
    }

    public void updateItemVisited(String itemId) {
        jobManager.addJobInBackground(new UpdateItemVisitedJob(itemId));
    }

    public void updateVideo(final Video video, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(video);
            }
        });
    }

}
