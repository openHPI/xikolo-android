package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import de.xikolo.BuildConfig;
import de.xikolo.dataaccess.HttpConnectionRequest;
import de.xikolo.dataaccess.HttpRequest;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;

public abstract class SessionManager {

    public static final String TAG = SessionManager.class.getSimpleName();

    private static boolean hasSession = false;

    private Context mContext;

    public SessionManager(Context context) {
        super();
        this.mContext = context;
    }

    public static boolean hasSession(Context context) {
        return hasSession;
    }

    public void createSession() {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "createSession() called");

        HttpRequest request = new HttpConnectionRequest(Config.URI_SAP + Config.PATH_LOGIN, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                HttpsURLConnection conn = (HttpsURLConnection) o;

                List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
                if (cookieList != null) for (String cookieTemp : cookieList) {
                    if (cookieTemp.startsWith("_normandy_session")) {
                        CookieManager.getInstance().setCookie(urlConnection.getURL().toString(), cookieTemp);
                        hasSession = true;
                    }
                    onSessionRequestReceived();
                }
            }

            @Override
            public void onRequestCancelled() {
                onSessionRequestCancelled();
            }
        };
        request.setCache(false);
        request.setToken(TokenManager.getAccessToken(mContext));
        request.setMethod(Config.HTTP_POST);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onSessionRequestReceived();

    public abstract void onSessionRequestCancelled();

}
