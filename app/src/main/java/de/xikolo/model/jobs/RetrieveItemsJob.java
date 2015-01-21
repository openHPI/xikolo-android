package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class RetrieveItemsJob extends Job {

    public static final String TAG = RetrieveItemsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Item>> result;
    private Course course;
    private Module module;
    private ItemDataAccess itemDataAccess;

    public RetrieveItemsJob(Result<List<Item>> result, Course course, Module module, ItemDataAccess itemDataAccess) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.course = course;
        this.module = module;
        this.itemDataAccess = itemDataAccess;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + course.id + " | module.id " + module.id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance()) || !course.is_enrolled) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            result.success(itemDataAccess.getAllItemsForModule(module), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                Type type = new TypeToken<List<Item>>(){}.getType();

                String url = Config.API + Config.COURSES + course.id + "/"
                        + Config.MODULES + module.id + "/" + Config.ITEMS;

                JsonRequest request = new JsonRequest(url, type);
                request.setToken(UserModel.getToken(GlobalApplication.getInstance()));

                Object o = request.getResponse();
                if (o != null) {
                    List<Item> items = (List<Item>) o;
                    if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                    for (Item item : items) {
                        itemDataAccess.addOrUpdateItem(module, item);
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
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
