package de.xikolo.model;

import com.path.android.jobqueue.JobManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.jobs.RetrieveItemDetailJob;
import de.xikolo.model.jobs.RetrieveItemsJob;
import de.xikolo.model.jobs.UpdateProgressionJob;
import de.xikolo.model.jobs.UpdateVideoJob;

public class ItemModel extends BaseModel {

    public static final String TAG = ItemModel.class.getSimpleName();

    public ItemModel(JobManager jobManager) {
        super(jobManager);
    }

    public void getItems(Result<List<Item>> result, Course course, Module module) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Item> onFilter(List<Item> result, Result.DataSource dataSource) {
                sortItems(result);
                return result;
            }
        });

        mJobManager.addJobInBackground(new RetrieveItemsJob(result, course, module));
    }

    public void getItemDetail(Result<Item> result, Course course, Module module, Item item, String itemType) {
        mJobManager.addJobInBackground(new RetrieveItemDetailJob(result, course, module, item, itemType));
    }

    public void updateProgression(Result<Void> result, Module module, Item item) {
        mJobManager.addJobInBackground(new UpdateProgressionJob(result, module, item));
    }

    public void updateVideo(Result<Void> result, VideoItemDetail videoItemDetail) {
        if(videoItemDetail.progress > 0) {
            mJobManager.addJobInBackground(new UpdateVideoJob(result, videoItemDetail));
        } else {
        }
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
