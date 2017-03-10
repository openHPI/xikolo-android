package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.controllers.exceptions.WrongParameterException;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.VideoItemDetail;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.storages.databases.adapters.VideoDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveItemDetailJob extends Job {

    public static final String TAG = RetrieveItemDetailJob.class.getSimpleName();

    private String courseId;
    private String moduleId;
    private String itemId;
    private String itemType;
    private Result<Item> result;

    public RetrieveItemDetailJob(Result<Item> result, String courseId, String moduleId, String itemId, String itemType) {
        super(new Params(Priority.HIGH));

        if (courseId == null || moduleId == null || itemId == null) {
            throw new WrongParameterException();
        }

        this.result = result;
        this.courseId = courseId;
        this.moduleId = moduleId;
        this.itemId = itemId;

        this.itemType = itemType;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | course.id " + courseId + " | module.id " + moduleId + " | item.id " + itemId + " | itemType " + itemType);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isAuthorized()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ItemDataAdapter itemDataAdapter = (ItemDataAdapter) GlobalApplication.getDataAdapter(DataType.ITEM);
            VideoDataAdapter videoDataAdapter = (VideoDataAdapter) GlobalApplication.getDataAdapter(DataType.VIDEO);

            if (itemType.equals(Item.TYPE_VIDEO)) {
                Item item = itemDataAdapter.get(itemId);
                if (item != null) {
                    item.detail = videoDataAdapter.get(itemId);
                    if (item.detail != null) {
                        result.success(item, Result.DataSource.LOCAL);
                    }
                }
            }

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS + itemId;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Item item = ApiParser.parse(response, Item.getType(itemType));
                    response.close();

                    item.courseId = courseId;
                    item.moduleId = moduleId;

                    if (Config.DEBUG) Log.i(TAG, "ItemDetail received");

                    if (itemType.equals(Item.TYPE_VIDEO)) {
                        itemDataAdapter.addOrUpdate(item);
                        videoDataAdapter.addOrUpdate((VideoItemDetail) item.detail);
                        // get local video progress, if available
                        item.detail = videoDataAdapter.get(item.id);
                    }

                    result.success(item, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No ItemDetail received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                if (itemType.equals(Item.TYPE_VIDEO)) {
                    result.warn(Result.WarnCode.NO_NETWORK);
                } else {
                    result.error(Result.ErrorCode.NO_NETWORK);
                }
            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
