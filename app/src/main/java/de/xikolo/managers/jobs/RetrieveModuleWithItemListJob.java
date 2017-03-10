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
import de.xikolo.models.Module;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.storages.databases.adapters.ModuleDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveModuleWithItemListJob extends Job {

    public static final String TAG = RetrieveModuleWithItemListJob.class.getSimpleName();

    private String courseId;
    private String moduleId;
    private Result<Module> result;

    public RetrieveModuleWithItemListJob(Result<Module> result, String courseId, String moduleId) {
        super(new Params(Priority.MID));

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
        if (!UserManager.isAuthorized()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ModuleDataAdapter moduleDataAdapter = (ModuleDataAdapter) GlobalApplication.getDataAdapter(DataType.MODULE);
            ItemDataAdapter itemDataAdapter = (ItemDataAdapter) GlobalApplication.getDataAdapter(DataType.ITEM);

            Module localModule = moduleDataAdapter.get(moduleId);
            if (localModule != null) {
                localModule.items = itemDataAdapter.getAllForModule(moduleId);
            }

            if (localModule != null && localModule.items != null) {
                result.success(localModule, Result.DataSource.LOCAL);
            }

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + "/" + moduleId;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Module module = ApiParser.parse(response, Module.class);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Module received (" + module.id + ")");

                    module.courseId = courseId;
                    moduleDataAdapter.addOrUpdate(module, false);

                    String itemListUrl = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS;

                    response = new ApiRequest(itemListUrl).execute();
                    if (response.isSuccessful()) {
                        Type type = TypeToken.getParameterized(List.class, Item.class).getType();
                        List<Item> items = ApiParser.parse(response, type);
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
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
