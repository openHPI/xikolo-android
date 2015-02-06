package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.entities.Module;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class RetrieveModulesJob extends Job {

    public static final String TAG = RetrieveModulesJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<List<Module>> result;
    private Course course;
    private boolean includeProgress;
    private ModuleDataAccess moduleDataAccess;

    public RetrieveModulesJob(Result<List<Module>> result, Course course, boolean includeProgress, ModuleDataAccess moduleDataAccess) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.course = course;
        this.includeProgress = includeProgress;
        this.moduleDataAccess = moduleDataAccess;
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
            result.success(moduleDataAccess.getAllModulesForCourse(course), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                Type type = new TypeToken<List<Module>>(){}.getType();

                String url = Config.API + Config.COURSES + course.id + "/"
                        + Config.MODULES + "?include_progress=" + includeProgress;

                JsonRequest request = new JsonRequest(url, type);
                request.setToken(UserModel.getToken(GlobalApplication.getInstance()));

                Object o = request.getResponse();
                if (o != null) {
                    List<Module> modules = (List<Module>) o;
                    if (Config.DEBUG) Log.i(TAG, "Modules received (" + modules.size() + ")");

                    for (Module module : modules) {
                        moduleDataAccess.addOrUpdateModule(course, module);
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
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
