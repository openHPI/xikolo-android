package de.xikolo;

import android.app.Application;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.managers.WebSocketManager;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.adapters.DataAdapter;
import de.xikolo.utils.ClientUtil;
import de.xikolo.utils.Config;
import de.xikolo.utils.FeatureToggle;
import de.xikolo.utils.SslCertificateUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class GlobalApplication extends Application {

    public static final String TAG = GlobalApplication.class.getSimpleName();

    private static GlobalApplication instance;

    private DatabaseHelper databaseHelper;

    private Lanalytics lanalytics;

    private WebSocketManager webSocketManager;

    private SecondScreenManager secondScreenManager;

    public GlobalApplication() {
        instance = this;
    }

    public static GlobalApplication getInstance() {
        return instance;
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

    public static DataAdapter getDataAdapter(DataType type) {
        return getInstance().getDatabaseHelper().getDataAdapter(type);
    }

    public DatabaseHelper getDatabaseHelper() {
        synchronized (GlobalApplication.class) {
            if (databaseHelper == null) {
                databaseHelper = new DatabaseHelper(this);
            }
        }
        return databaseHelper;
    }

    public String getClientId() {
        return ClientUtil.id(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configureRealm();
        configureDefaultSettings();
        configureWebView();
        configureSecondScreenManager();

        // just for debugging, never use for production
        if (Config.DEBUG) {
            SslCertificateUtil.disableSslCertificateChecking();
        }
    }

    private void configureRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    private void configureDefaultSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
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

    public void configureSecondScreenManager() {
        if (FeatureToggle.secondScreen()) {
            synchronized (GlobalApplication.class) {
                if (secondScreenManager == null) {
                    secondScreenManager = new SecondScreenManager();
                }
            }
        }
    }

}
