package de.xikolo.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.managers.jobs.RetrieveItemDetailJob;
import de.xikolo.managers.jobs.RetrieveItemListJob;
import de.xikolo.managers.jobs.RetrieveLocalVideoJob;
import de.xikolo.managers.jobs.RetrieveVideoSubtitlesJob;
import de.xikolo.managers.jobs.UpdateLocalVideoJob;
import de.xikolo.managers.jobs.UpdateProgressionJob;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Subtitle;
import de.xikolo.models.VideoItemDetail;

public class ItemManager extends BaseManager {

    public static final String TAG = ItemManager.class.getSimpleName();

    public void getItems(Result<List<Item>> result, Course course, Section module) {
        getItems(result, course.id, module.id);
    }

    public void getItems(Result<List<Item>> result, String courseId, String moduleId) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Item> onFilter(List<Item> result, Result.DataSource dataSource) {
                sortItems(result);
                return result;
            }
        });

        jobManager.addJobInBackground(new RetrieveItemListJob(result, courseId, moduleId));
    }

    public void getItemDetail(Result<Item> result, Course course, Section module, Item item, String itemType) {
        getItemDetail(result, course.id, module.id, item.id, itemType);
    }

    public void getItemDetail(Result<Item> result, String courseId, String moduleId, String itemId, String itemType) {
        jobManager.addJobInBackground(new RetrieveItemDetailJob(result, courseId, moduleId, itemId, itemType));
    }

    public void getVideoSubtitles(Result<List<Subtitle>> result, String courseId, String moduleId, String videoId) {
        jobManager.addJobInBackground(new RetrieveVideoSubtitlesJob(result, courseId, moduleId, videoId));
    }

    public void updateProgression(Result<Void> result, Item item) {
        jobManager.addJobInBackground(new UpdateProgressionJob(result, item));
    }

    public void updateLocalVideoProgress(Result<Void> result, VideoItemDetail videoItemDetail) {
        jobManager.addJobInBackground(new UpdateLocalVideoJob(result, videoItemDetail));
    }

    public void getLocalVideoProgress(Result<VideoItemDetail> result, VideoItemDetail videoItemDetail) {
            jobManager.addJobInBackground(new RetrieveLocalVideoJob(result, videoItemDetail.id));
    }

    public static void sortItems(List<Item> items) {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return lhs.position - rhs.position;
            }
        });
    }

}
