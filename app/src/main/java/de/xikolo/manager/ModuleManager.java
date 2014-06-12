package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.dataaccess.JsonRequest;
import de.xikolo.model.Course;
import de.xikolo.model.Module;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;
import de.xikolo.util.Network;
import de.xikolo.util.Toaster;

public abstract class ModuleManager {

    public static final String TAG = ModuleManager.class.getSimpleName();

    private Context mContext;

    public ModuleManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestModules(Course course, boolean cache) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestModules() called | cache " + cache);

        Type type = new TypeToken<List<Module>>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_COURSES + course.id + "/"
                + Config.PATH_MODULES, type, mContext) {
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
