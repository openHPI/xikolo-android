package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.base.Sync;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class ListItemsWithContentForSectionJob extends BaseJob {

    public static final String TAG = ListItemsWithContentForSectionJob.class.getSimpleName();

    private String sectionId;

    public ListItemsWithContentForSectionJob(JobCallback callback, String sectionId) {
        super(new Params(PRIORITY_HIGH), callback);

        this.sectionId = sectionId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | section.id " + sectionId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {

                Response<Item.JsonModel[]> response =
                        ApiService.getInstance().listItemsWithContentForSection(sectionId).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Items received");

                    Sync.Data.with(Item.class, response.body())
                            .addFilter("sectionId", sectionId)
                            .run();
                    syncItemContent(response.body());

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
