package de.xikolo;

import android.app.Application;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import de.xikolo.config.Config;
import de.xikolo.config.FeatureToggle;
import de.xikolo.models.base.RealmSchemaMigration;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.managers.WebSocketManager;
import de.xikolo.utils.ClientUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();

    private static App instance;

    private Lanalytics lanalytics;

    private WebSocketManager webSocketManager;

    private SecondScreenManager secondScreenManager;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public Lanalytics getLanalytics() {
        synchronized (App.class) {
            if (lanalytics == null) {
                lanalytics = Lanalytics.getInstance(this, Config.API_URL + Config.LANALYTICS_PATH);
            }
        }
        return lanalytics;
    }

    public WebSocketManager getWebSocketManager() {
        synchronized (App.class) {
            if (webSocketManager == null) {
                webSocketManager = new WebSocketManager(Config.WEBSOCKET_URL);
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

        configureRealm();
        configureDefaultSettings();
        configureWebView();
        configureSecondScreenManager();
    }

    private void configureRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(Config.REALM_SCHEMA_VERSION)
                .migration(new RealmSchemaMigration())
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

    @SuppressWarnings("deprecation")
    public void clearCookieSyncManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.getInstance();
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        } else  {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
        }
    }

    public void configureSecondScreenManager() {
        if (FeatureToggle.secondScreen()) {
            synchronized (App.class) {
                if (secondScreenManager == null) {
                    secondScreenManager = new SecondScreenManager();
                }
            }
        }
    }

}
