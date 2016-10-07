package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.entities.Item;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveItemsJob extends Job {

    public static final String TAG = RetrieveItemsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Item>> result;
    private String courseId;
    private String moduleId;

    public RetrieveItemsJob(Result<List<Item>> result, String courseId, String moduleId) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

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
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ItemDataAccess itemDataAccess = GlobalApplication.getInstance()
                    .getDataAccessFactory().getItemDataAccess();
            result.success(itemDataAccess.getAllItemsForModule(moduleId), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + moduleId + "/" + Config.ITEMS;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<List<Item>>() {}.getType();
                    List<Item> items = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                    for (Item item : items) {
                        itemDataAccess.addOrUpdateItem(moduleId, item);
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
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
