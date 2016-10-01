package de.xikolo.controller;

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
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;
import com.path.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.preferences.NotificationPreferences;
import de.xikolo.data.preferences.PreferencesFactory;
import de.xikolo.model.UserModel;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.model.events.PermissionDeniedEvent;
import de.xikolo.model.events.PermissionGrantedEvent;
import de.xikolo.model.receiver.NotificationDeletedReceiver;
import de.xikolo.util.FeatureToggle;
import de.xikolo.util.PlayServicesUtil;

public abstract class BaseActivity extends AppCompatActivity {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    protected AppBarLayout appBar;

    protected VideoCastManager videoCastManager;

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

        PlayServicesUtil.checkPlayServices(this);

        videoCastManager = VideoCastManager.getInstance();

        VideoCastConsumerImpl castConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (castPresent) {
                    showOverlay();
                }
            }
        };
        videoCastManager.addVideoCastConsumer(castConsumer);

        if (overlay == null) {
            showOverlay();
        }

        handleIntent(getIntent());
    }

    private void showOverlay() {
        if (overlay != null) {
            overlay.remove();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaRouteMenuItem != null && mediaRouteMenuItem.isVisible()) {
                    overlay = new IntroductoryOverlay.Builder(BaseActivity.this)
                            .setMenuItem(mediaRouteMenuItem)
                            .setTitleText(R.string.intro_overlay_text)
                            .setSingleTime()
                            .setOnDismissed(new IntroductoryOverlay.OnOverlayDismissedListener() {
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

        if (UserModel.isLoggedIn(this) && FeatureToggle.secondScreen()) {
            globalApplication.getWebSocketManager().initConnection(UserModel.getToken(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalApplication.startCookieSyncManager();
        videoCastManager = VideoCastManager.getInstance();
        videoCastManager.incrementUiCounter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        globalApplication.syncCookieSyncManager();
        globalApplication.stopCookieSyncManager();
        videoCastManager.decrementUiCounter();
    }

    @Override
    protected void onStop() {
        super.onStop();

        globalApplication.flushHttpResponseCache();

        EventBus.getDefault().unregister(this);
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
        getMenuInflater().inflate(R.menu.cast, menu);
        mediaRouteMenuItem = videoCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        PreferencesFactory preferencesFactory = new PreferencesFactory(this);
        NotificationPreferences notificationPreferences = preferencesFactory.getNotificationPreferences();

        String title = intent.getStringExtra(NotificationDeletedReceiver.KEY_TITLE);
        if (title != null) {
            notificationPreferences.deleteDownloadNotification(title);
        } else if (intent.getStringExtra(NotificationDeletedReceiver.KEY_ALL) != null) {
            notificationPreferences.deleteAllDownloadNotifications();
        }
    }

}
