package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Module;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ModuleDataAdapter;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

public class RetrieveModuleListJob extends Job {

    public static final String TAG = RetrieveModuleListJob.class.getSimpleName();

    private String courseId;
    private boolean includeProgress;
    private Result<List<Module>> result;

    public RetrieveModuleListJob(Result<List<Module>> result, String courseId, boolean includeProgress) {
        super(new Params(Priority.MID));

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
        if (!UserManager.isAuthorized()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            ModuleDataAdapter moduleDataAccess = (ModuleDataAdapter) GlobalApplication.getDataAdapter(DataType.MODULE);
            List<Module> localModules = moduleDataAccess.getAllForCourse(courseId);

            if (includeProgress) {
                List<Module> deleteList = new ArrayList<>();
                for (Module module : localModules) {
                    if (module.progress == null) {
                        deleteList.add(module);
                    }
                }
                localModules.removeAll(deleteList);
            }
            result.success(localModules, Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                        + Config.MODULES + "?include_progress=" + includeProgress;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = TypeToken.getParameterized(List.class, Module.class).getType();
                    List<Module> modules = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "Modules received (" + modules.size() + ")");

                    for (Module module : modules) {
                        module.courseId = courseId;
                        moduleDataAccess.addOrUpdate(module, includeProgress);
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
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
