package de.xikolo.model.jobs;

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
import de.xikolo.data.database.DataAccessFactory;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveModulesWithItemsJob extends Job {

    public static final String TAG = RetrieveModulesWithItemsJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Module>> result;
    private Course course;
    private boolean includeProgress;

    public RetrieveModulesWithItemsJob(Result<List<Module>> result, Course course, boolean includeProgress) {
        super(new Params(Priority.MID));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.course = course;
        this.includeProgress = includeProgress;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | includeProgress " + includeProgress + " | course.id " + course.id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance()) || !course.is_enrolled) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            DataAccessFactory dataAccessFactory = GlobalApplication.getInstance().getDataAccessFactory();
            ModuleDataAccess moduleDataAccess = dataAccessFactory.getModuleDataAccess();
            ItemDataAccess itemDataAccess = dataAccessFactory.getItemDataAccess();

            List<Module> localModules = moduleDataAccess.getAllModulesForCourse(course);
            List<Module> deleteList = new ArrayList<>();
            for (Module module : localModules) {
                if (includeProgress && module.progress == null) {
                    deleteList.add(module);
                } else {
                    module.items = itemDataAccess.getAllItemsForModule(module.id);
                }
            }
            localModules.removeAll(deleteList);
            result.success(localModules, Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + course.id + "/"
                        + Config.MODULES + "?include_progress=" + includeProgress;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<List<Module>>(){}.getType();
                    List<Module> modules = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Modules received (" + modules.size() + ")");

                    for (Module module : modules) {
                        moduleDataAccess.addOrUpdateModule(course.id, module, includeProgress);
                    }

                    for (Module module : modules) {
                        String itemListUrl = Config.API + Config.COURSES + course.id + "/"
                                + Config.MODULES + module.id + "/" + Config.ITEMS;

                        response = new ApiRequest(itemListUrl).execute();
                        if (response.isSuccessful()) {
                            Type itemListType = new TypeToken<List<Item>>() {}.getType();
                            List<Item> items = ApiParser.parse(response, itemListType);
                            response.close();

                            if (Config.DEBUG) Log.i(TAG, "Items received (" + items.size() + ")");

                            for (Item item : items) {
                                itemDataAccess.addOrUpdateItem(module.id, item);
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
