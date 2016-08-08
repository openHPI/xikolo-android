package de.xikolo.model;

import com.path.android.jobqueue.JobManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.Subtitle;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.jobs.RetrieveItemDetailJob;
import de.xikolo.model.jobs.RetrieveItemsJob;
import de.xikolo.model.jobs.RetrieveLocalVideoJob;
import de.xikolo.model.jobs.RetrieveVideoSubtitlesJob;
import de.xikolo.model.jobs.UpdateProgressionJob;
import de.xikolo.model.jobs.UpdateLocalVideoJob;

public class ItemModel extends BaseModel {

    public static final String TAG = ItemModel.class.getSimpleName();

    public ItemModel(JobManager jobManager) {
        super(jobManager);
    }

    public void getItems(Result<List<Item>> result, Course course, Module module) {
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

        mJobManager.addJobInBackground(new RetrieveItemsJob(result, courseId, moduleId));
    }

    public void getItemDetail(Result<Item> result, Course course, Module module, Item item, String itemType) {
        getItemDetail(result, course.id, module.id, item.id, itemType);
    }

    public void getItemDetail(Result<Item> result, String courseId, String moduleId, String itemId, String itemType) {
        mJobManager.addJobInBackground(new RetrieveItemDetailJob(result, courseId, moduleId, itemId, itemType));
    }

    public void getVideoSubtitles(Result<List<Subtitle>> result, String courseId, String moduleId, String videoId) {
        mJobManager.addJobInBackground(new RetrieveVideoSubtitlesJob(result, courseId, moduleId, videoId));
    }

    public void updateProgression(Result<Void> result, Module module, Item item) {
        mJobManager.addJobInBackground(new UpdateProgressionJob(result, module, item));
    }

    public void updateLocalVideoProgress(Result<Void> result, VideoItemDetail videoItemDetail) {
        mJobManager.addJobInBackground(new UpdateLocalVideoJob(result, videoItemDetail));
    }

    public void getLocalVideoProgress(Result<VideoItemDetail> result, VideoItemDetail videoItemDetail) {
            mJobManager.addJobInBackground(new RetrieveLocalVideoJob(result, videoItemDetail));
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
