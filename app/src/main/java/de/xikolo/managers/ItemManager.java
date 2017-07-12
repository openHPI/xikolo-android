package de.xikolo.managers;

import de.xikolo.jobs.GetItemWithContentJob;
import de.xikolo.jobs.ListItemsWithContentForSectionJob;
import de.xikolo.jobs.ListSubtitlesWithTextsJob;
import de.xikolo.jobs.UpdateItemVisitedJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Item;
import de.xikolo.models.Video;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class ItemManager extends BaseManager {

    public static final String TAG = ItemManager.class.getSimpleName();

    public RealmResults listAccessibleItemsForSection(String sectionId, Realm realm, RealmChangeListener<RealmResults<Item>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Item> itemListPromise = realm
                .where(Item.class)
                .equalTo("sectionId", sectionId)
                .equalTo("accessible", true)
                .findAllSortedAsync("position");

        itemListPromise.addChangeListener(listener);

        return itemListPromise;
    }

    public RealmObject getVideoForItem(String itemId, Realm realm, RealmChangeListener<Video> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmObject videoPromise = realm
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

    public void requestSubtitlesWithTextsForVideo(String videoId, JobCallback callback) {
        jobManager.addJobInBackground(new ListSubtitlesWithTextsJob(callback, videoId));
    }

    public void updateItemVisited(String itemId) {
        jobManager.addJobInBackground(new UpdateItemVisitedJob(itemId));
    }

    public void updateVideoProgress(final Video video, final int progress, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                video.progress = progress;
                realm.copyToRealmOrUpdate(video);
            }
        });
    }

}
