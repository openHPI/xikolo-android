package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.BuildConfig;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.preferences.EnrollmentsPreferences;
import de.xikolo.model.Enrollment;
import de.xikolo.util.BuildType;
import de.xikolo.util.Network;
import de.xikolo.util.Path;

public class ProgressionManager {

    public static final String TAG = ProgressionManager.class.getSimpleName();

    private Context mContext;

    public ProgressionManager(Context context) {
        super();
        this.mContext = context;
    }

    public void updateProgression(String itemId) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "updateProgression() called | itemId " + itemId);

        HttpRequest request = new HttpRequest(Path.API_SAP + Path.USER + Path.PROGRESSIONS + itemId, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.i(TAG, "Progression done");
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "Progression not done");
            }
        };
        request.setMethod(Path.HTTP_PUT);
        request.setToken(TokenManager.getAccessToken(mContext));
        request.setCache(false);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
