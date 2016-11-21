package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveItemListJob extends Job {

    public static final String TAG = RetrieveItemListJob.class.getSimpleName();

    private String courseId;
    private String moduleId;
    private Result<List<Item>> result;

    public RetrieveItemListJob(Result<List<Item>> result, String courseId, String moduleId) {
        super(new Params(Priority.MID));

        this.result = result;
        this.courseId = courseId;
        this.moduleId = moduleId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId + " | module.id " + moduleId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ItemDataAdapter itemDataAdapter = (ItemDataAdapter) GlobalApplication.getDataAdapter(DataType.ITEM);
            result.success(itemDataAdapter.getAllForModule(moduleId), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + moduleId + "/" + Config.ITEMS;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = TypeToken.getParameterized(List.class, Item.class).getType();
                    List<Item> items = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                    for (Item item : items) {
                        item.courseId = courseId;
                        item.moduleId = moduleId;
                        itemDataAdapter.addOrUpdate(item);
                    }

                    result.success(items, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No Item received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                result.warn(Result.WarnCode.NO_NETWORK);
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
