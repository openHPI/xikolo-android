package de.xikolo.managers;

import de.xikolo.jobs.GetItemWithContentJob;
import de.xikolo.jobs.ListItemsWithContentForSectionJob;
import de.xikolo.jobs.ListSectionsWithItemsJob;
import de.xikolo.jobs.ListSubtitlesWithCuesJob;
import de.xikolo.jobs.UpdateItemVisitedJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Item;
import de.xikolo.models.RichText;
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

    public RealmResults listAccessibleItemsForCourse(String courseId, Realm realm, RealmChangeListener<RealmResults<Item>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Item> itemListPromise = realm
                .where(Item.class)
                .equalTo("courseId", courseId)
                .equalTo("accessible", true)
                .findAllAsync();

        itemListPromise.addChangeListener(listener);

        return itemListPromise;
    }

    public RealmResults getVideoForItem(String contentId, Realm realm, RealmChangeListener<RealmResults<Video>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        // RealmChangeListener for RealmObject doesn't notify for initial copyToRealm
        RealmResults<Video> videoPromise = realm
                .where(Video.class)
                .equalTo("id", contentId)
                .findAllAsync();

        videoPromise.addChangeListener(listener);

        return videoPromise;
    }

    public RealmResults getRichTextForItem(String contentId, Realm realm, RealmChangeListener<RealmResults<RichText>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        // RealmChangeListener for RealmObject doesn't notify for initial copyToRealm
        RealmResults<RichText> richTextPromise = realm
                .where(RichText.class)
                .equalTo("id", contentId)
                .findAllAsync();

        richTextPromise.addChangeListener(listener);

        return richTextPromise;
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

    public void requestSectionListWithItems(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new ListSectionsWithItemsJob(courseId, callback));
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
