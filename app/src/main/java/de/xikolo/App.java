package de.xikolo;

import android.app.Application;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.preference.PreferenceManager;
import de.xikolo.config.Config;
import de.xikolo.config.FeatureToggle;
import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.managers.WebSocketManager;
import de.xikolo.models.migrate.RealmSchemaMigration;
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

    private void configureWebView() {
        CookieManager.getInstance().setAcceptCookie(true);

        if (Config.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public void syncCookieSyncManager() {
        CookieManager.getInstance().flush();
    }

    public void clearCookieSyncManager() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().removeSessionCookies(null);
        CookieManager.getInstance().flush();
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
