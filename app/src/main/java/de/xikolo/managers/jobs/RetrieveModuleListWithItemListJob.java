package de.xikolo.managers.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.storages.databases.adapters.ModuleDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveModuleListWithItemListJob extends Job {

    public static final String TAG = RetrieveModuleListWithItemListJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String courseId;
    private boolean includeProgress;
    private Result<List<Module>> result;

    public RetrieveModuleListWithItemListJob(Result<List<Module>> result, String courseId, boolean includeProgress) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.courseId = courseId;
        this.includeProgress = includeProgress;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | includeProgress " + includeProgress + " | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ModuleDataAdapter moduleDataAdapter = (ModuleDataAdapter) GlobalApplication.getDataAdapter(DataType.MODULE);
            ItemDataAdapter itemDataAdapter = (ItemDataAdapter) GlobalApplication.getDataAdapter(DataType.ITEM);

            List<Module> localModules = moduleDataAdapter.getAllForCourse(courseId);
            List<Module> deleteList = new ArrayList<>();
            for (Module module : localModules) {
                if (includeProgress && module.progress == null) {
                    deleteList.add(module);
                } else {
                    module.items = itemDataAdapter.getAllForModule(module.id);
                }
            }
            localModules.removeAll(deleteList);
            result.success(localModules, Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + "?include_progress=" + includeProgress;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<List<Module>>(){}.getType();
                    List<Module> modules = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Modules received (" + modules.size() + ")");

                    for (Module module : modules) {
                        module.courseId = courseId;
                        moduleDataAdapter.addOrUpdate(module, includeProgress);
                    }

                    for (Module module : modules) {
                        String itemListUrl = Config.API + Config.COURSES + courseId+ "/"
                                + Config.MODULES + module.id + "/" + Config.ITEMS;

                        response = new ApiRequest(itemListUrl).execute();
                        if (response.isSuccessful()) {
                            Type itemListType = new TypeToken<List<Item>>() {}.getType();
                            List<Item> items = ApiParser.parse(response, itemListType);
                            response.close();

                            if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                            for (Item item : items) {
                                item.courseId = courseId;
                                item.moduleId = module.id;
                                itemDataAdapter.addOrUpdate(item);
                            }

                            module.items = items;
                        } else {
                            if (Config.DEBUG) Log.w(TAG, "No Item received");
                            module.items = null;
                        }
                    }

                    result.success(modules, Result.DataSource.NETWORK);
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
