package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListSectionsWithItemsJob extends BaseJob {

    public static final String TAG = ListSectionsWithItemsJob.class.getSimpleName();

    private String courseId;

    public ListSectionsWithItemsJob(String courseId, JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);

        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {

                Response<Section.JsonModel[]> response =
                        ApiService.getInstance().listSectionsWithItemsForCourse(courseId).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Sections received");

                    Sync.Data.with(Section.class, response.body())
                            .addFilter("courseId", courseId)
                            .run();
                    Sync.Included.with(Item.class, response.body())
                            .addFilter("courseId", courseId)
                            .run();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching section list");
                    if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
                }
            } else {
                if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
