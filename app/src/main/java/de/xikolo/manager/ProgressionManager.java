package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import de.xikolo.BuildConfig;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;

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

        HttpRequest request = new HttpRequest(Config.API_SAP + Config.USER + Config.PROGRESSIONS + itemId, mContext) {
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
        request.setMethod(Config.HTTP_PUT);
        request.setToken(TokenManager.getAccessToken(mContext));
        request.setCache(false);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
