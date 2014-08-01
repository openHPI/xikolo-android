package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.BuildConfig;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.model.Course;
import de.xikolo.model.Module;
import de.xikolo.util.BuildType;
import de.xikolo.util.Network;
import de.xikolo.util.Path;

public abstract class ModuleManager {

    public static final String TAG = ModuleManager.class.getSimpleName();

    private Context mContext;

    public ModuleManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestModules(Course course, boolean cache, boolean includeProgress) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestModules() called | cache " + cache + "| includeProgress " + includeProgress);

        Type type = new TypeToken<List<Module>>() {
        }.getType();

        String url = Path.API_SAP + Path.COURSES + course.id + "/"
                + Path.MODULES + "?include_progress=" + includeProgress;

        JsonRequest request = new JsonRequest(url, type, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (o != null) {
                    List<Module> modules = (List<Module>) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "Modules received (" + modules.size() + ")");
                    onModulesRequestReceived(modules);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No Modules received");
                    onModulesRequestCancelled();
                }
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "Modules Request cancelled");
                onModulesRequestCancelled();
            }
        };
        request.setCache(cache);
        if (!Network.isOnline(mContext) && cache) {
            request.setCacheOnly(true);
        }
        request.setToken(TokenManager.getAccessToken(mContext));
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onModulesRequestReceived(List<Module> modules);

    public abstract void onModulesRequestCancelled();

}
