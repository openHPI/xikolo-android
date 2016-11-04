package de.xikolo.controllers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.events.PermissionDeniedEvent;
import de.xikolo.events.PermissionGrantedEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.receivers.NotificationDeletedReceiver;
import de.xikolo.storages.preferences.NotificationStorage;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.utils.FeatureToggle;
import de.xikolo.utils.PlayServicesUtil;

public abstract class BaseActivity extends AppCompatActivity implements CastStateListener {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    protected AppBarLayout appBar;

    protected CastContext castContext;

    private DrawerLayout drawerLayout;

    private CoordinatorLayout contentLayout;

    private MenuItem mediaRouteMenuItem;

    private boolean offlineModeToolbar;

    private IntroductoryOverlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        offlineModeToolbar = true;

        if (PlayServicesUtil.checkPlayServices(getApplicationContext())) {
            castContext = CastContext.getSharedInstance(this);
        }

        if (overlay == null) {
            showOverlay();
        }

        handleIntent(getIntent());
    }

    private void showOverlay() {
        if (overlay != null) {
            overlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaRouteMenuItem != null && mediaRouteMenuItem.isVisible()) {
                        overlay = new IntroductoryOverlay.Builder(BaseActivity.this, mediaRouteMenuItem)
                                .setTitleText(R.string.intro_overlay_text)
                                .setSingleTime()
                                .setOnOverlayDismissedListener(new IntroductoryOverlay.OnOverlayDismissedListener() {
                                    @Override
                                    public void onOverlayDismissed() {
                                        overlay = null;
                                    }
                                })
                                .build();
                        overlay.show();
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onCastStateChanged(int newState) {
        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showOverlay();
        }
    }

    protected void setupActionBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
        }

        toolbar = tb;

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        contentLayout = (CoordinatorLayout) findViewById(R.id.contentLayout);
        appBar = (AppBarLayout) findViewById(R.id.appbar);
        setColorScheme(R.color.apptheme_main, R.color.apptheme_main_dark);
    }

    protected boolean setupCastMiniController() {
        if (PlayServicesUtil.checkPlayServices(this) && findViewById(R.id.miniControllerContainer) != null) {
            View container = findViewById(R.id.miniControllerContainer);
            ViewGroup parent = (ViewGroup) container.getParent();
            int index = parent.indexOfChild(container);

            parent.removeView(container);
            container = getLayoutInflater().inflate(R.layout.container_mini_controller, parent, false);
            parent.addView(container, index);

            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unused")
    protected void setActionBarElevation(float elevation) {
        if (actionBar != null && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(elevation);
        }
    }

    protected void setAppBarExpanded(boolean expanded) {
        if (appBar != null) {
            appBar.setExpanded(expanded, false);
        }
    }

    protected void enableOfflineModeToolbar(boolean enable) {
        this.offlineModeToolbar = enable;
    }

    protected void setColorScheme(int color, int darkColor) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, color));
            if (Build.VERSION.SDK_INT >= 21) {
                if (drawerLayout != null) {
                    drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, color));
                } else {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, darkColor));
                }
                if (contentLayout != null) {
                    contentLayout.setBackgroundColor(ContextCompat.getColor(this, color));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkStateEvent event) {
        if (toolbar != null && offlineModeToolbar) {
            if (event.isOnline()) {
                toolbar.setSubtitle("");
                setColorScheme(R.color.apptheme_main, R.color.apptheme_main_dark);
            } else {
                toolbar.setSubtitle(getString(R.string.offline_mode));
                setColorScheme(R.color.offline_mode_actionbar, R.color.offline_mode_statusbar);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        if (castContext != null) {
            castContext.addCastStateListener(this);
            setupCastMiniController();
        }

        if (UserManager.isLoggedIn() && FeatureToggle.secondScreen()) {
            globalApplication.getWebSocketManager().initConnection(UserManager.getToken());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalApplication.startCookieSyncManager();
    }

    @Override
    protected void onPause() {
        super.onPause();

        globalApplication.syncCookieSyncManager();
        globalApplication.stopCookieSyncManager();
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

        if (castContext != null) {
            castContext.removeCastStateListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            EventBus.getDefault().post(new PermissionGrantedEvent(requestCode));

        } else {

            EventBus.getDefault().post(new PermissionDeniedEvent(requestCode));

        }
    }

    @SuppressWarnings("unused")
    protected void enableCastMediaRouterButton(boolean enable) {
        if (mediaRouteMenuItem != null) {
            mediaRouteMenuItem.setVisible(enable);
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (PlayServicesUtil.checkPlayServices(getApplicationContext())) {
            getMenuInflater().inflate(R.menu.cast, menu);
            mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
                    getApplicationContext(),
                    menu,
                    R.id.media_route_menu_item);
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        NotificationStorage notificationStorage = (NotificationStorage) GlobalApplication.getStorage(StorageType.NOTIFICATION);

        String title = intent.getStringExtra(NotificationDeletedReceiver.KEY_TITLE);
        if (title != null) {
            notificationStorage.deleteDownloadNotification(title);
        } else if (intent.getStringExtra(NotificationDeletedReceiver.KEY_ALL) != null) {
            notificationStorage.deleteAllDownloadNotifications();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (PlayServicesUtil.checkPlayServices(getApplicationContext())) {
            return CastContext.getSharedInstance(this)
                    .onDispatchVolumeKeyEventBeforeJellyBean(event)
                    || super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

}
