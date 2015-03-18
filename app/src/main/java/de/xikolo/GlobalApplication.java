package de.xikolo;

import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.HttpResponseCache;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.entities.Download;
import de.xikolo.data.net.DownloadHelper;
import de.xikolo.model.events.DownloadCompletedEvent;
import de.xikolo.util.Config;
import de.xikolo.util.SslCertificateUtil;

public class GlobalApplication extends Application {

    public static final String TAG = GlobalApplication.class.getSimpleName();

    private static GlobalApplication instance;

    private JobManager jobManager;

    private HttpResponseCache httpResponseCache;

    private DatabaseHelper databaseHelper;

    private CookieSyncManager cookieSyncManager;

    public GlobalApplication() {
        instance = this;
    }

    public static GlobalApplication getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configureDefaultSettings();
        configureFontTypefaces();
        configureDatabase();
        configureImageLoader();
        configureHttpResponseCache();
        configureWebViewCookies();
        configureJobManager();
        registerDownloadBroadcastReceiver();

        // just for debugging, never use for production
        if (Config.DEBUG) {
            SslCertificateUtil.disableSslCertificateChecking();
        }
    }

    private void configureDefaultSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }

    private void configureFontTypefaces() {
        // Set global Typefaces
        // Doesn't work on Android 5 anymore
//        FontsOverride.setDefaultFont(this, "SANS_SERIF", Config.FONT_SANS);
    }

    private void configureDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void configureImageLoader() {
        // Create ImageLoader configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(false)
                .showImageOnLoading(R.drawable.gradient_default_image)
                .showImageForEmptyUri(R.drawable.gradient_default_image)
                .showImageOnFail(R.drawable.gradient_default_image)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheSize(30 * 1024 * 1024) // 30 MB
                .memoryCache(new LruMemoryCache(10 * 1024 * 1024)) // 10 MB
                .memoryCacheSize(10 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(config);
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

    private void configureWebViewCookies() {
        // Enable WebView Cookies
        cookieSyncManager = CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
    }

    private void configureJobManager() {
        int numThreads = Runtime.getRuntime().availableProcessors();

        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
//                        return Config.DEBUG;
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
                .minConsumerCount(1) // always keep at least one consumer alive
                .maxConsumerCount(numThreads) // consumers at a time
                .loadFactor(2) // jobs per consumer
                .consumerKeepAlive(120) // wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);

        if (Config.DEBUG) {
            Log.i(TAG, "CPU Cores: " + Runtime.getRuntime().availableProcessors());
        }
    }

    public void flushHttpResponseCache() {
        if (httpResponseCache != null) {
            httpResponseCache.flush();
        }
    }

    public void startCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.startSync();
        }
    }

    public void stopCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.stopSync();
        }
    }

    public void syncCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.sync();
        }
    }

    private void registerDownloadBroadcastReceiver() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    Download dl = DownloadHelper.getDownloadForId(downloadId);
                    if (dl != null) {
                        EventBus.getDefault().post(new DownloadCompletedEvent(dl));
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

}
