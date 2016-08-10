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
import de.xikolo.data.database.DataAccessFactory;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class RetrieveModuleWithItemsJob extends Job {

    public static final String TAG = RetrieveModuleWithItemsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Module> result;
    private String courseId;
    private String moduleId;

    public RetrieveModuleWithItemsJob(Result<Module> result, String courseId, String moduleId) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.courseId = courseId;
        this.moduleId = moduleId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            DataAccessFactory dataAccessFactory = GlobalApplication.getInstance().getDataAccessFactory();
            ModuleDataAccess moduleDataAccess = dataAccessFactory.getModuleDataAccess();
            ItemDataAccess itemDataAccess = dataAccessFactory.getItemDataAccess();

            Module localModule = moduleDataAccess.getModule(moduleId);
            localModule.items = itemDataAccess.getAllItemsForModule(moduleId);

            result.success(localModule, Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                Type type = new TypeToken<Module>(){}.getType();

                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + "/" + moduleId;

                JsonRequest request = new JsonRequest(url, type);
                request.setCache(false);

                request.setToken(UserModel.getToken(GlobalApplication.getInstance()));

                Object o = request.getResponse();
                if (o != null) {
                    @SuppressWarnings("unchecked")
                    Module module = (Module) o;
                    if (Config.DEBUG) Log.i(TAG, "Module received (" + module.id + ")");

                    moduleDataAccess.addOrUpdateModule(courseId, module, false);

                    Type typeItemList = new TypeToken<List<Item>>() {}.getType();

                    String urlItemList = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS;

                    JsonRequest requestItemList = new JsonRequest(urlItemList, typeItemList);
                    requestItemList.setCache(false);

                    requestItemList.setToken(UserModel.getToken(GlobalApplication.getInstance()));

                    Object oItemList = requestItemList.getResponse();
                    if (oItemList != null) {
                        @SuppressWarnings("unchecked")
                        List<Item> items = (List<Item>) oItemList;
                        if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                        for (Item item : items) {
                            itemDataAccess.addOrUpdateItem(module.id, item);
                        }

                        module.items = items;
                    } else {
                        if (Config.DEBUG) Log.w(TAG, "No Item received");
                        module.items = null;
                    }

                    result.success(module, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No Modules received");
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
