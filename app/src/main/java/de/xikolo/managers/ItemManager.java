package de.xikolo.managers;

import de.xikolo.jobs.GetItemWithContentJob;
import de.xikolo.jobs.ListItemsWithContentForSectionJob;
import de.xikolo.jobs.ListSubtitlesWithCuesJob;
import de.xikolo.jobs.UpdateItemVisitedJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Item;
import de.xikolo.models.Video;
import io.realm.Realm;
import io.realm.RealmChangeListener;
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

    public RealmResults getVideoForItem(String itemId, Realm realm, RealmChangeListener<RealmResults<Video>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        // RealmChangeListener for RealmObject doesn't notify for initial copyToRealm
        RealmResults<Video> videoPromise = realm
                .where(Video.class)
                .equalTo("itemId", itemId)
                .findAllAsync();

        videoPromise.addChangeListener(listener);

        return videoPromise;
    }

    public void requestItemWithContent(String itemId, JobCallback callback) {
        jobManager.addJobInBackground(new GetItemWithContentJob(callback, itemId));
    }

    public void requestItemsWithContentForSection(String sectionId, JobCallback callback) {
        jobManager.addJobInBackground(new ListItemsWithContentForSectionJob(callback, sectionId));
    }

    public void requestSubtitlesWithCuesForVideo(String videoId, JobCallback callback) {
        jobManager.addJobInBackground(new ListSubtitlesWithCuesJob(callback, videoId));
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
