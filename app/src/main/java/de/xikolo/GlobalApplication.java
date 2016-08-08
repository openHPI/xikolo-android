package de.xikolo;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.io.File;
import java.io.IOException;

import de.xikolo.data.database.DataAccessFactory;
import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.preferences.PreferencesFactory;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.managers.WebSocketManager;
import de.xikolo.util.ClientUtil;
import de.xikolo.util.Config;
import de.xikolo.util.SslCertificateUtil;

public class GlobalApplication extends Application {

    public static final String TAG = GlobalApplication.class.getSimpleName();

    private static GlobalApplication instance;

    private JobManager jobManager;

    private HttpResponseCache httpResponseCache;

    private DatabaseHelper databaseHelper;

    private DataAccessFactory dataAccessFactory;

    private PreferencesFactory preferencesFactory;

    private Lanalytics lanalytics;

    private WebSocketManager webSocketManager;

    private SecondScreenManager secondScreenManager;

    public GlobalApplication() {
        instance = this;
    }

    public static GlobalApplication getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public DataAccessFactory getDataAccessFactory() {
        synchronized (GlobalApplication.class) {
            if (dataAccessFactory == null) {
                dataAccessFactory = new DataAccessFactory(databaseHelper);
            }
        }
        return dataAccessFactory;
    }

    public PreferencesFactory getPreferencesFactory() {
        synchronized (GlobalApplication.class) {
            if (preferencesFactory == null) {
                preferencesFactory = new PreferencesFactory(this);
            }
        }
        return preferencesFactory;
    }

    public Lanalytics getLanalytics() {
        synchronized (GlobalApplication.class) {
            if (lanalytics == null) {
                lanalytics = Lanalytics.getInstance(this, Config.API_V2 + Config.LANALYTICS);
            }
        }
        return lanalytics;
    }

    public WebSocketManager getWebSocketManager() {
        synchronized (GlobalApplication.class) {
            if (webSocketManager == null) {
                webSocketManager = new WebSocketManager(Config.WEBSOCKET);
            }
        }
        return webSocketManager;
    }

    public String getClientId() {
        return ClientUtil.id(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configureDefaultSettings();
        configureDatabase();
        configureHttpResponseCache();
        configureWebView();
        configureJobManager();
        configureVideoCastManager();

        configureSecondScreenManager();

        // just for debugging, never use for production
        if (Config.DEBUG) {
            SslCertificateUtil.disableSslCertificateChecking();
        }
    }

    private void configureDefaultSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }

    private void configureDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void configureHttpResponseCache() {
        // Create HTTP Response Cache
        try {
            File httpCacheDir = new File(this.getCacheDir(), "http");
            long httpCacheSize = 20 * 1024 * 1024; // 20 MiB
            httpResponseCache = HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i(TAG, "HTTP Response Cache installation failed:" + e);
        }
    }

    @SuppressWarnings("deprecation")
    private void configureWebView() {
        // Enable WebView Cookies
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        CookieManager.getInstance().setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Config.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private void configureJobManager() {
        int numThreads = Runtime.getRuntime().availableProcessors();

        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(numThreads > 3 ? 3 : numThreads) // always keep at least one consumer alive
                .maxConsumerCount(numThreads) // consumers at a time
                .loadFactor(2) // jobs per consumer
                .consumerKeepAlive(120) // wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);
    }

    public void flushHttpResponseCache() {
        if (httpResponseCache != null) {
            httpResponseCache.flush();
        }
    }

    @SuppressWarnings("deprecation")
    public void startCookieSyncManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @SuppressWarnings("deprecation")
    public void stopCookieSyncManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    @SuppressWarnings("deprecation")
    public void syncCookieSyncManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
    }

    private void configureVideoCastManager() {
        CastConfiguration options = new CastConfiguration.Builder(Config.CAST_MEDIA_RECEIVER_APPLICATION_ID)
                .enableAutoReconnect()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .setNextPrevVisibilityPolicy(CastConfiguration.NEXT_PREV_VISIBILITY_POLICY_HIDDEN)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .setCastControllerImmersive(false)
                .build();
        VideoCastManager.initialize(this, options);
    }

    public void configureSecondScreenManager() {
        synchronized (GlobalApplication.class) {
            if (secondScreenManager == null) {
                secondScreenManager = new SecondScreenManager();
            }
        }
    }

}
